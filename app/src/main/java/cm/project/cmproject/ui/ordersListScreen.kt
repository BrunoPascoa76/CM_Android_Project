package cm.project.cmproject.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.ui.theme.CMProjectTheme
import cm.project.cmproject.ui.theme.PlaceAnOrderScreenTheme

/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CMProjectTheme {
                OrdersListScreen()
            }
        }
    }
}
*/

@Composable
fun OrdersListScreen(modifier: Modifier = Modifier, navController: NavController = rememberNavController()) {
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }

    if (shouldShowOnboarding) {
        Column {
            OnboardingScreen(
                navController = navController
            )
            Greetings()
        }
    }else{
        Greetings()
    }
}

@Composable
fun Greetings(modifier: Modifier = Modifier, names: List<String> = List (1000){"$it"}) {
    Column(modifier = Modifier.padding(vertical = 4.dp)){
        LazyColumn {
            items(names) { name ->
                Greeting(name = name) }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val expanded = remember { mutableStateOf(false) }

    ElevatedButton(modifier = Modifier.padding(12.dp).fillMaxWidth(), onClick = {expanded.value = !expanded.value}, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary), contentPadding = PaddingValues(vertical = 15.dp, horizontal = 100.dp)) {
        Text(text = "Order n.º $name")
    }
}

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, navController: NavController = rememberNavController()/*, onSearch: (String) -> Unit*/) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Post & Paket Tracking")
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = {
                navController.navigate("profile") //TODO
            }
        ) {
            Text("Create New Order")
        }
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Tracking Number") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                //onSearch(text)
                // Hide the keyboard after submitting the search
                keyboardController?.hide()
                //or hide keyboard
                focusManager.clearFocus()

            })
        )
        Box (modifier = Modifier.padding(vertical = 24.dp)){
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                Text("My Orders")
                Greetings()
            }
        }
    }
}

/*
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    PlaceAnOrderScreenTheme {
        OnboardingScreen(onContinueClicked = {}/*, onSearch = {}*/)
    }
}
*/

@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingPreview() {
    PlaceAnOrderScreenTheme {
        OrdersListScreen()
    }
}