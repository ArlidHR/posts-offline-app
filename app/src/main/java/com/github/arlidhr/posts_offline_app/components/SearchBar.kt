package com.github.arlidhr.posts_offline_app.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Reusable search bar composable with search icon and clear button.
 *
 * @param query Current search text.
 * @param onQueryChange Callback invoked when the text changes.
 * @param modifier Modifier for the text field.
 * @param placeholder Hint text shown when the field is empty.
 */
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun AppSearchBarEmptyPreview() {
    AppSearchBar(
        query = "",
        onQueryChange = {},
        placeholder = "Search by name or ID..."
    )
}

@Preview(showBackground = true)
@Composable
private fun AppSearchBarWithTextPreview() {
    AppSearchBar(
        query = "kotlin",
        onQueryChange = {}
    )
}
