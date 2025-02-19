import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Update
import com.bumptech.glide.Glide
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.diseaseName.DiseaseInfo
import com.example.imagerecognitionapp.data.diseaseName.diseaseDatabase
import com.example.imagerecognitionapp.data.historyItem.HistoryAdapter
import com.example.imagerecognitionapp.data.historyItem.HistoryItem
import com.example.imagerecognitionapp.data.model.History
import com.example.imagerecognitionapp.databinding.FragmentHistoryBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.history.HistoryViewModel
import com.example.imagerecognitionapp.ui.result.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    }

    private fun EnabledRetroceso(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
            }
        })
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
            binding.recyclerViewHistory.visibility = if (histories.isNotEmpty()) View.VISIBLE else View.GONE
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
            onExitClick = { requireActivity().finish() }
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
        super.onDestroyView()
        //sharedViewModel.clearSelectedHistory()
        _binding = null
    }
}
