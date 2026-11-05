package studio.deapps.demail.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import studio.deapps.demail.R

// ---------- Colors ----------
val Ink = Color.Black
val Ink50 = Color.Black.copy(alpha = 0.5f)
val ScreenBackground = Color(0xFFEFEFEF)
val CardColor = Color.White

// ---------- Font ----------
val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_bold, FontWeight.Bold),
)

// ---------- Text styles used by the inbox ----------
object DeType {
    /** "Inbox" header – bigger than the sender line. */
    val Header = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = Ink,
    )

    /** Sender – bold, black, biggest line in the card. */
    val Sender = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Ink,
    )

    /** Subject – bold, 50% black, a bit smaller than sender. */
    val Subject = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Ink50,
    )

    /** Body preview + timestamp – regular, 50% black, smallest. */
    val Body = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Ink50,
    )

    /** Selection counter in the action bar. */
    val Count = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Ink,
    )
}

private fun outfitTypography(): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = Outfit),
        displayMedium = base.displayMedium.copy(fontFamily = Outfit),
        displaySmall = base.displaySmall.copy(fontFamily = Outfit),
        headlineLarge = base.headlineLarge.copy(fontFamily = Outfit),
        headlineMedium = base.headlineMedium.copy(fontFamily = Outfit),
        headlineSmall = base.headlineSmall.copy(fontFamily = Outfit),
        titleLarge = base.titleLarge.copy(fontFamily = Outfit),
        titleMedium = base.titleMedium.copy(fontFamily = Outfit),
        titleSmall = base.titleSmall.copy(fontFamily = Outfit),
        bodyLarge = base.bodyLarge.copy(fontFamily = Outfit),
        bodyMedium = base.bodyMedium.copy(fontFamily = Outfit),
        bodySmall = base.bodySmall.copy(fontFamily = Outfit),
        labelLarge = base.labelLarge.copy(fontFamily = Outfit),
        labelMedium = base.labelMedium.copy(fontFamily = Outfit),
        labelSmall = base.labelSmall.copy(fontFamily = Outfit),
    )
}

private val DeMailColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    background = ScreenBackground,
    onBackground = Ink,
    surface = CardColor,
    onSurface = Ink,
)

@Composable
fun DeMailTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DeMailColors,
        typography = outfitTypography(),
        content = content,
    )
}
