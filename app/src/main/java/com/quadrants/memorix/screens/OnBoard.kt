package com.quadrants.memorix.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.tooling.preview.Preview
import com.quadrants.memorix.OnBoardingData
import com.quadrants.memorix.R
import com.quadrants.memorix.LoaderIntro
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.MediumViolet
import com.quadrants.memorix.ui.theme.GoldenYellow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardScreen(
    onFinish: () -> Unit // Callback for navigation
) {
    val items = listOf(
        OnBoardingData(
            R.raw.scrolling,
            "No More Doom Scrolling... Well, Almost! 📱",
            "Now, you can doom scroll… but on your studies! 📖📚"
        ),
        OnBoardingData(
            R.raw.studying,
            "Studying is Now Fun! 🎮",
            "Swipe flashcards, chat with AI, take quizzes—learning has never been this easy (or fun!)."
        ),
        OnBoardingData(
            R.raw.exams,
            "Ace Your Exams Like a Pro! 🏆",
            "Memorix keeps you on track so you can ace your subjects with confidence. (No more last-minute cramming—or tears! 😭)"
        )
    )

    val pagerState = rememberPagerState(
        pageCount = { items.size },
        initialPage = 0
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet), // Apply primary background color
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(1000))) {
                OnBoardingPage(items[page])
            }
        }

        // Page Indicator
        PageIndicator(size = items.size, currentPage = pagerState.currentPage)

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom Section with Navigation Controls
        BottomSection(
            currentPage = pagerState.currentPage,
            pageCount = items.size,
            onFinish = onFinish
        )
    }
}

@Composable
fun OnBoardingPage(data: OnBoardingData) {
    Column(
        modifier = Modifier
            .padding(40.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Loader / Placeholder for Image
        LoaderIntro(
            modifier = Modifier
                .size(250.dp)
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally),
            image = data.image
        )

        // Title
        Text(
            text = data.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GoldenYellow, // Use accent color for text
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )

        // Description
        Text(
            text = data.desc,
            fontSize = 14.sp,
            color = Color.White, // White text for readability
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp)
        )
    }
}

@Composable
fun PageIndicator(size: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(size) {
            Indicator(isSelected = it == currentPage)
        }
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    val width: Dp = if (isSelected) 25.dp else 10.dp

    Box(
        modifier = Modifier
            .padding(4.dp)
            .height(10.dp)
            .width(width)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) GoldenYellow else MediumViolet) // Gold for active, Purple for inactive
    )
}

@Composable
fun BottomSection(currentPage: Int, pageCount: Int, onFinish: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 50.dp),
        horizontalArrangement = if (currentPage != pageCount - 1) Arrangement.SpaceBetween else Arrangement.Center
    ) {
        if (currentPage == pageCount - 1) {
            OutlinedButton(
                onClick = onFinish,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkViolet)
            ) {
                Text(
                    text = "Get Started",
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 40.dp),
                    color = DarkViolet // Button text
                )
            }
        } else {
            SkipNextButton("Skip")
            SkipNextButton("Next")
        }
    }
}

@Composable
fun SkipNextButton(text: String) {
    Text(
        text = text,
        color = GoldenYellow, // Accent color for buttons
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardScreen() {
    OnboardScreen(onFinish = {})
}
