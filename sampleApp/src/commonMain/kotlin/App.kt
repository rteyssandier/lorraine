import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.dot.lorraine.Lorraine
import io.dot.lorraine.dsl.startLorraine
import io.dot.lorraine.models.LorraineContext
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.TestScreen
import ui.TestViewModel
import worker.DeleteWorker
import worker.GetWorker
import worker.PatchWorker
import worker.PostWorker
import worker.PutWorker

const val GET_WORKER = "GET_WORKER"
const val POST_WORKER = "POST_WORKER"
const val PUT_WORKER = "PUT_WORKER"
const val PATCH_WORKER = "PATCH_WORKER"
const val DELETE_WORKER = "DELETE_WORKER"

 var lorraine: Lorraine? = null

fun initLorraine(context: LorraineContext) {
    lorraine = startLorraine(context) {
        work(GET_WORKER) { GetWorker() }
        work(POST_WORKER) { PostWorker() }
        work(PATCH_WORKER) { PatchWorker() }
        work(PUT_WORKER) { PutWorker() }
        work(DELETE_WORKER) { DeleteWorker() }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = remember { TestViewModel() }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.padding(it)
            ) {
                TestScreen(viewModel)
            }
        }
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

val client = HttpClient(engine()) {
    expectSuccess = true

    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                println("Test: $message")
            }
        }
    }

    install(ContentNegotiation) {
        json(json)
    }
}