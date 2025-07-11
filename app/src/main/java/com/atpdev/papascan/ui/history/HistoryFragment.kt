import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.atpdev.papascan.R
import com.atpdev.papascan.data.historyItem.HistoryAdapter
import com.atpdev.papascan.data.historyItem.HistoryItem
import com.atpdev.papascan.databinding.FragmentHistoryBinding
import com.atpdev.papascan.ui.common.MenuToolbar
import com.atpdev.papascan.ui.dialog.FragmentAlertDialogExit
import com.atpdev.papascan.ui.history.HistoryViewModel
import com.atpdev.papascan.ui.result.SharedViewModel
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch


class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var menuHandler: MenuToolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        EnabledRetroceso()
        setupClearAllButton() // Agrega esta línea

    }

    private fun EnabledRetroceso(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
            }
        })
    }

    private fun setupClearAllButton() {
        val btnClearAll = binding.btndeleteallhistory
        val lottieAnimation = binding.btnDeleteallhistorylottieAnimationView

        btnClearAll.setOnClickListener {
            // Mostrar diálogo de confirmación
            showClearHistoryDialog(lottieAnimation)
        }
    }

    private fun showToastCorrect(message: String){
        StyleableToast.makeText(requireContext(), message, R.style.exampleToastCorrect).show()
    }

    private fun showClearHistoryDialog(lottieAnimation: LottieAnimationView) {
        FragmentAlertDialogExit.newInstance(
            title = "Vaciar historial",
            message = "¿Estás seguro de que quieres eliminar todo el historial?",
            positiveText = "Eliminar todo",
            negativeText = "Cancelar",
            onPositive = {
                // Animación al confirmar
                lottieAnimation.speed = 1.5f
                lottieAnimation.playAnimation()

                lottieAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        lifecycleScope.launch {
                            try {
                                // Vaciar el historial
                                historyViewModel.clearAllHistory()
                                // Actualizar UI
                                binding.recyclerViewHistory.visibility = View.GONE
                                binding.noHistoryTextView.visibility = View.VISIBLE
                                // Mostrar confirmación
                                showToastCorrect("Historial vaciado")
                            } catch (e: Exception) {
                                Log.e("HistoryFragment", "Error al vaciar historial", e)
                                Toast.makeText(
                                    requireContext(),
                                    "Error al vaciar el historial",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                lottieAnimation.frame = 0
                            }
                        }
                    }
                })
            },
            onNegative = {
                // No se necesita acción específica al cancelar
            }
        ).show(parentFragmentManager, "ClearHistoryDialog")
    }

    // Método para obtener los detalles de la enfermedad
    /*private suspend fun getDiseaseInfo(diseaseName: String): DiseaseInfo {
        // Aquí deberías hacer alguna consulta a la base de datos o repositorio
        return diseaseDatabase[diseaseName] ?: DiseaseInfo(
            name = diseaseName,
            description = "Descripción no disponible",
            prevention = "Prevención no disponible",
            causes = "Causas no disponible",
            treatment = "Tratamiento no disponible"
        )
    }*/

    private fun setupRecyclerView() {
            historyAdapter = HistoryAdapter(
                onDeleteClick = { historyItem ->
                    //historyViewModel.removeFromHistory(historyItem.diseaseName)
                    //Log.d("HistoryFragment", "Eliminando historial con nombre de enfermedad: ${historyItem.diseaseName}")
                    // Eliminar por id
                    historyViewModel.removeFromHistoryById(historyItem.id)  // Llama a un método que elimina por id
                    Log.d("HistoryFragment", "Eliminando historial con id: ${historyItem.id}")

                },
                onItemClick = { historyItem ->
                    lifecycleScope.launch {
                        // Obtener el historial más actualizado directamente de la base de datos
                        val updatedHistory = historyViewModel.getHistoryById(historyItem.id)
                        Log.d("HistoryFragment", "ACTUALIZED ${updatedHistory}")
                        updatedHistory?.let { //history ->
                            // Clear previous data first
                            // En lugar de pasar `null`, pasamos una instancia con valores por defecto
                            //sharedViewModel.clearSelectedHistory()
                            sharedViewModel.setSelectedHistoryItem(it)
                            // Forzar una recarga del historial
                            //historyViewModel.getAllHistory()
                            findNavController().navigate(R.id.action_historyFragment_to_diseaseInfoFragment)
                        }
                       }
                }
            )

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }


    /*private fun setupRecyclerView() {
        // Inicializar el adapter con la función de eliminar
        historyAdapter = HistoryAdapter { historyItem ->
            // Llamar al método removeFromHistory del ViewModel
            historyViewModel.removeFromHistory(historyItem.diseaseName, historyItem.section)
        }
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }*/

    /*private fun observeViewModel() {
        historyViewModel.historyList.observe(viewLifecycleOwner) { historyItems ->
            historyAdapter.submitList(historyItems)
        }
    }*/
    // En el Fragment
    private fun observeViewModel() {
        historyViewModel.historyList.observe(viewLifecycleOwner) { histories ->
            Log.d("HistoryFragment", "Actualizando lista: ${histories.size} elementos")

            val historyItems = histories.map { history ->
                HistoryItem(
                    id = history.id,
                    diseaseName = history.diseaseName,
                    section = history.section,
                    timestamp = history.timestamp,
                    imagePath = history.imagePath ?: ""
                )
            }

            historyAdapter.submitList(historyItems.toList())
            //binding.recyclerViewHistory.visibility = if (histories.isNotEmpty()) View.VISIBLE else View.GONE

            // Mostrar/ocultar RecyclerView y texto de historial vacío
            val hasHistory = histories.isNotEmpty()
            binding.recyclerViewHistory.visibility = if (hasHistory) View.VISIBLE else View.GONE
            binding.noHistoryTextView.visibility = if (hasHistory) View.GONE else View.VISIBLE

            // Habilitar/deshabilitar botón según si hay historial
            binding.btndeleteallhistory.isEnabled = hasHistory
            binding.btnDeleteallhistorylottieAnimationView.isEnabled = hasHistory

            // Cambiar opacidad para indicar estado deshabilitado
            val alphaValue = if (hasHistory) 1.0f else 0.5f
            binding.btndeleteallhistory.alpha = alphaValue
            binding.btnDeleteallhistorylottieAnimationView.alpha = alphaValue
        }

        /*historyViewModel.historyList.observe(viewLifecycleOwner) { histories ->
            Log.d("UI", "Se actualizó la lista del historial: ${histories.size} elementos -> Datos: $histories")
            val historyItems = histories.map { history ->
                HistoryItem(
                    diseaseName = history.diseaseName,
                    section = "main",
                    timestamp = history.timestamp,
                    imagePath = history.imagePath ?: ""
                )
            }
            historyAdapter.submitList(historyItems)
            Log.d("UI", "Lista enviada al adaptador: ${historyItems.size} elementos")
            binding.recyclerViewHistory.visibility =
                if (histories.isNotEmpty()) View.VISIBLE else View.GONE
        }*/
    }



    private fun setupToolbar() {
        menuHandler = MenuToolbar(
            context = requireContext(),
            onHistoryClick = { /* Ya estamos en History */ },
            onAboutClick = { navigateToAlertDialog() },
            onExitClick = { showExitConfirmationDialog() }
        )

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.appBarMenu.toolbar)
            supportActionBar?.apply {
                title = "Historial"
                setDisplayHomeAsUpEnabled(true)
            }
        }
        setHasOptionsMenu(true)
    }

    private fun showExitConfirmationDialog() {
        FragmentAlertDialogExit.newInstance(
            title = getString(R.string.exit_app_title),
            message = getString(R.string.exit_app_message),
            positiveText = getString(R.string.exit),
            negativeText = getString(R.string.cancel),
            onPositive = {
                cleanupResources()
                requireActivity().finishAffinity()
            },
            onNegative = {
                // No hacer nada o puedes agregar lógica adicional
            }
        ).show(parentFragmentManager, "ExitDialog")
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuHandler.onCreateOptionsMenu(menu, inflater)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //findNavController().navigateUp()
                findNavController().navigate(R.id.action_historyFragment_to_recognitionFragment)
                true
            }
            else -> menuHandler.onOptionsItemSelected(item)
        }
    }
    /*private fun showClearHistoryDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_history_title)
            .setMessage(R.string.clear_history_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }*/

    private fun navigateToAlertDialog() {
        findNavController().navigate(R.id.action_historyFragment_to_fragmentAlertDialog)
    }

    override fun onDestroyView() {
        cleanupResources()
        super.onDestroyView()
        //sharedViewModel.clearSelectedHistory()
    }

    private fun cleanupResources() {
        try {
            // 1. Limpiar el adaptador del RecyclerView
            binding.recyclerViewHistory.adapter = null

            // 2. Cancelar corrutinas pendientes
            lifecycleScope.launch {
                coroutineContext.cancelChildren()
            }

            // 3. Limpiar selección en el ViewModel compartido
            sharedViewModel.clearSelectedHistory()

            // 4. Detener y limpiar animaciones Lottie
            binding.btnDeleteallhistorylottieAnimationView.apply {
                cancelAnimation()
                setImageDrawable(null)
            }

            // 5. Remover listeners
            binding.btndeleteallhistory.setOnClickListener(null)

            // 6. Limpiar Glide (si estás usando imágenes)
            Glide.with(this).clear(binding.root)

            // 7. Liberar binding
            _binding = null

            Log.d("HistoryFragment", "All resources cleaned up")
        } catch (e: Exception) {
            Log.e("HistoryFragment", "Error during cleanup", e)
        }
    }
}
