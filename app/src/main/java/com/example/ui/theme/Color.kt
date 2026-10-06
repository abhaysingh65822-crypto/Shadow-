package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// STUDYFORGE OS DESIGN SYSTEM PALETTE
// Cyber-Academic Deep Space / Neomorphic Luminescence Palette
// =========================================================================

// Primary Brand & Electric Accents
val ForgeIndigo = Color(0xFF6366F1)
val ForgeIndigoLight = Color(0xFF818CF8)
val ForgeIndigoDark = Color(0xFF4338CA)
val ForgeViolet = Color(0xFFA855F7)
val ForgeVioletDark = Color(0xFF7E22CE)

// High-Energy Semantic Accents
val ForgeCyan = Color(0xFF06B6D4)
val ForgeCyanLight = Color(0xFF38BDF8)
val ForgeAmber = Color(0xFFF59E0B)
val ForgeEmerald = Color(0xFF10B981)
val ForgeEmeraldLight = Color(0xFF34D399)
val ForgeRose = Color(0xFFF43F5E)
val ForgeCoral = Color(0xFFFB7185)

// Deep Futuristic OS Canvas Surfaces (Dark Mode Dominance)
val DarkBg = Color(0xFF090D16)
val DarkSurface = Color(0xFF111726)
val DarkSurfaceVariant = Color(0xFF1A233A)
val DarkSurfaceGlass = Color(0xCC131B2E)
val DarkBorder = Color(0xFF24324F)
val DarkBorderSubtle = Color(0x334F6E9D)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Clean Crisp Light Mode
val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDF2F7)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)

// High-Tech Gradient Brushes for OS Surfaces
val ForgeCosmicBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4))
)

val ForgeHeroGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF182238), Color(0xFF0F1626))
)

val ForgeCardGlassBrush = Brush.linearGradient(
    colors = listOf(Color(0x336366F1), Color(0x1006B6D4), Color(0x22111827))
)

val ForgeAccentGlow = Brush.radialGradient(
    colors = listOf(Color(0x40818CF8), Color(0x00000000))
)

// Legacy M3 compatibility
val Purple80 = ForgeIndigoLight
val PurpleGrey80 = Color(0xFFCBD5E1)
val Pink80 = ForgeCyanLight
val Purple40 = ForgeIndigo
val PurpleGrey40 = Color(0xFF475569)
val Pink40 = ForgeCyan
