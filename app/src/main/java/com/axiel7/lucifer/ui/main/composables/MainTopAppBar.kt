package com.axiel7.lucifer.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.axiel7.lucifer.R
import com.axiel7.lucifer.ui.base.navigation.Route

@Composable
fun MainTopAppBar(
    profilePicture: String?,
    isVisible: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = isVisible,
        transitionSpec = {
            slideInVertically(initialOffsetY = { -it }) togetherWith
                    slideOutVertically(targetOffsetY = { -it })
        }
    ) { isVisibleState ->
        if (isVisibleState) {
            // 🚀 COMPLETELY TRANSPARENT ROW INSTEAD OF A SOLID CARD
            Row(
                modifier = modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // Pushes icons to the edges!
                verticalAlignment = Alignment.CenterVertically
            ) {

                // LEFT: SEARCH ICON
                IconButton(
                    onClick = dropUnlessResumed { navController.navigate(Route.Search()) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_search_24),
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // RIGHT: PROFILE ICON
                AsyncImage(
                    model = profilePicture,
                    contentDescription = "Profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(100))
                        .clickable { navController.navigate(Route.Profile) }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}