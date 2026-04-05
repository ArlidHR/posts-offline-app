package com.github.arlidhr.posts_offline_app.modules.comments.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment

/**
 * Composable displaying an individual comment as a Material 3 card.
 *
 * Visually distinguishes between:
 * - **API comments**: outlined person icon, shows email
 * - **Local user comments**: filled person icon + "You" badge, no email
 *
 * Fully parameterized — no ViewModel dependency, easy to test and preview.
 *
 * @param comment The comment data to display.
 * @param modifier Optional modifier for the card container.
 */
@Composable
fun CommentItem(
    comment: Comment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = if (comment.isLocal) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: icon + author name + optional "You" badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (comment.isLocal) Icons.Filled.Person
                    else Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (comment.isLocal) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                if (comment.isLocal) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "You",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Email (API comments only)
            if (comment.email.isNotBlank()) {
                Text(
                    text = comment.email,
                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment body
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "API Comment")
@Composable
private fun ApiCommentItemPreview() {
    CommentItem(
        comment = Comment(
            id = 1, postId = 1,
            name = "id labore ex et quam laborum",
            email = "Eliseo@gardner.biz",
            body = "laudantium enim quasi est quidem magnam voluptate ipsam eos",
            isLocal = false
        )
    )
}

@Preview(showBackground = true, name = "Local Comment")
@Composable
private fun LocalCommentItemPreview() {
    CommentItem(
        comment = Comment(
            id = 0, postId = 1,
            name = "My first comment",
            body = "This is a comment I created offline!",
            isLocal = true
        )
    )
}
