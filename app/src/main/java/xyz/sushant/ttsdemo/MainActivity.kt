package xyz.sushant.ttsdemo

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.earlypayment.ttsdemo.ui.theme.TTSDemoTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import java.lang.Exception
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var inputString = MutableLiveData("")
    private val lightBlue = Color(0xffd8e6ff)

    private val ttsTag = "TTS_LOG";

    private val localIdentifierList = listOf<String>("HI", "EN-us", "MAR", "FR")

    private val identifiersList = MutableLiveData<List<Locale>>()

    private var initializationError: Boolean = false

    private var latestSpeech = ""

    private val textToSpeech by lazy {
        TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS)
            identifiersList.postValue(textToSpeech.availableLanguages.toList())
        else
            initializationError = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TTSDemoTheme(darkTheme = false) {
                // A surface container using the 'background' color from the theme
                val languagesList = identifiersList.observeAsState()
                Column(
                    Modifier
                        .background(color = Color.White)
                        .fillMaxWidth(1f)
                        .fillMaxHeight(1f)
                ) {
                    TopAppBar {
                        Text(
                            text = "TTS DEMO COMPOSE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .fillMaxHeight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var inString by rememberSaveable { mutableStateOf("") }
                        var selectedLanguage by rememberSaveable { mutableStateOf(Locale.ENGLISH) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Language"
                            )
                            DropDownMenu(
                                selected = selectedLanguage.displayLanguage,
                                objectList = languagesList.value ?: listOf<Locale>(),
                                objectTransformer = { "${it.country} ${it.displayLanguage} " },
                                onSelectionChangedListener = { it, obj -> selectedLanguage = obj }
                            )
                        }

                        TextField(
                            value = inString,
                            onValueChange = {
                                inString = it
                                inputString.postValue(it)
                            },
                            placeholder = {
                                Text("Input your text here")
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = lightBlue,
                                cursorColor = Color.Black,
                                disabledLabelColor = lightBlue,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                if (inString.isNotEmpty()) {
                                    IconButton(onClick = { inString = "" }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { outputTTS(inString, selectedLanguage) }) {
                            Text(text = "Hear Voice")
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(
            ttsTag,
            "language ${Locale.ENGLISH.language}: ${textToSpeech.isLanguageAvailable(Locale.ENGLISH)}"
        )
        textToSpeech.isLanguageAvailable(Locale.ENGLISH)
    }

    private fun outputTTS(input: String, locale: Locale) {
        try {
            if (!initializationError) {
                val status = textToSpeech.setLanguage(locale)
                when (status) {
                    TextToSpeech.LANG_AVAILABLE -> {
                        Log.d(ttsTag,"Language Available")
                    }
                    TextToSpeech.LANG_COUNTRY_AVAILABLE -> {
                        Log.e(ttsTag,"Language Country available")
                    }
                    TextToSpeech.LANG_MISSING_DATA -> {
                        Log.e(ttsTag,"Language Missing Data")
                    }
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        Log.e(ttsTag,"Language not supported")
                    }
                }
                latestSpeech = input.hashCode().toString()
                textToSpeech.speak(input, TextToSpeech.QUEUE_FLUSH, null, latestSpeech)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun <T : Any> DropDownMenu(
    selected: String,
    onSelectionChangedListener: (it: String, obj: T) -> Unit,
    objectList: List<T>,
    objectTransformer: ((it: T) -> String)
) {

    Column {
        var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
        TextButton(
            onClick = {
                dropdownExpanded = true
            },
        ) {
            Text(selected)
        }
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
        ) {
            objectList.forEach { selection ->
                DropdownMenuItem(onClick = {
                    dropdownExpanded = false
                    onSelectionChangedListener(objectTransformer(selection), selection)
                }) {
                    Text(text = objectTransformer(selection))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!", fontSize = 14.sp)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    TTSDemoTheme {
        Greeting("Android")
    }
}