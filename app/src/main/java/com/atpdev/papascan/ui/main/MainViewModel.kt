package com.atpdev.papascan.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    application: Application,
    //private val imageRecognitionRepository: ImageRecognitionRepository,
    //private val cameraRepository: CameraRepository
) :AndroidViewModel(application){

    // LiveData para los resultados de reconocimiento de la imagen
    /*private val _recognitionResult = MutableLiveData<RecognitionResult>()
    val recognitionResult: LiveData<RecognitionResult> get() = _recognitionResult

    private val _bitmapFlow = MutableStateFlow<Bitmap?>(null)
    val bitmapFlow = _bitmapFlow.asStateFlow()

    private val _imageProxyFlow = MutableStateFlow<ImageProxy?>(null)
    val imageProxyFlow = _imageProxyFlow.asStateFlow()

    // - 1 = inicial   0 = error,  1 = exito
    private val _operacionExitosa = MutableLiveData<Int>()
    val operacionExitosa: LiveData<Int> get() = _operacionExitosa

    fun iniciar(){
        _operacionExitosa.value = -1
    }
    // LiveData para el estado de carga
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    // LiveData para manejar errores
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Función para procesar la imagen y obtener el resultado
    /*fun processImage(imageFile: File) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = imageRecognitionRepository.getRecognitionResult(imageFile)
                _recognitionResult.postValue(result)
                _loading.postValue(false)
            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
                _loading.postValue(false)
            }
        }
    }*/*/


}