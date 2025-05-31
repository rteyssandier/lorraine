import androidx.compose.ui.window.ComposeUIViewController
import io.dot.lorraine.dsl.createLorraineContext
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName")
fun MainViewController(): UIViewController {
    initLorraine(createLorraineContext())
    return ComposeUIViewController { App() }
}