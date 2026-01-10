@file:OptIn(ExperimentalLayoutApi::class, ExperimentalUuidApi::class)

package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.LaunchedEffectFlowWithLifecycle
import io.dot.lorraine.models.LorraineInfo
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun TestScreen(
    viewModel: TestViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val hostState = remember { SnackbarHostState() }

    LaunchedEffectFlowWithLifecycle(viewModel.event) { event ->
        when (event) {
            is TestEvent.Message -> hostState.showSnackbar(message = event.message)
        }
    }

    Content(
        uiState = uiState,
        hostState = hostState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun Content(
    uiState: TestUIState,
    hostState: SnackbarHostState,
    onAction: (TestAction) -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState) {
                Snackbar(it)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SuggestionChip(
                    label = { Text("GET") },
                    onClick = { onAction(TestAction.Send(MethodType.GET)) }
                )
                SuggestionChip(
                    label = { Text("POST") },
                    onClick = { onAction(TestAction.Send(MethodType.POST)) }
                )
                SuggestionChip(
                    label = { Text("PUT") },
                    onClick = { onAction(TestAction.Send(MethodType.PUT)) }
                )
                SuggestionChip(
                    label = { Text("DELETE") },
                    onClick = { onAction(TestAction.Send(MethodType.DELETE)) }
                )
                SuggestionChip(
                    label = { Text("PATCH") },
                    onClick = { onAction(TestAction.Send(MethodType.PATCH)) }
                )
                SuggestionChip(
                    label = { Text("OPERATION") },
                    onClick = { onAction(TestAction.Operation) }
                )
                SuggestionChip(
                    label = { Text("CLEAR ALL") },
                    onClick = { onAction(TestAction.Clear) }
                )
            }
            Text(
                text = "Lorraine Infos",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    uiState.info
                ) { item ->
                    ItemUI(item)
                }
            }
        }
    }
}

@Composable
private fun ItemUI(item: LorraineInfo) {
    val shape = remember { RoundedCornerShape(10.dp) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .shadow(4.dp, shape)
            .background(Color.White, shape)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Id: ${item.uuid}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Identifier: ${item.identifier}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            item.tags.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = item.state.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(4.dp)
        )
    }
}