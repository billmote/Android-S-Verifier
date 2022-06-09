package com.example.android.androidsverifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android.androidsverifier.ui.theme.AndroidSVerifierTheme
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSVerifierTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val contactKey = remember { mutableStateOf("") }
                    SFMCSdk.requestSdk { sdk ->
                        sdk.mp { mp ->
                            contactKey.value = mp.registrationManager.contactKey ?: ""
                        }
                    }
                    Greeting(name = contactKey.value)
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String
) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidSVerifierTheme {
        Greeting("Android")
    }
}