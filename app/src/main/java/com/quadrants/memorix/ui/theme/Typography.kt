package com.quadrants.memorix.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quadrants.memorix.R

// Define WorkSans Font Family
val WorkSans: FontFamily
    get() = FontFamily(
        Font(R.font.worksansregular, FontWeight.Normal),
        Font(R.font.worksansmedium, FontWeight.Medium),
        Font(R.font.worksansbold, FontWeight.Bold)
    )

// Set WorkSans as the Default Typography
val MemorixTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)
