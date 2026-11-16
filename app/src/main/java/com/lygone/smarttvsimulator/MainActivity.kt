package com.lygone.smarttvsimulator

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF080D19)
private val Panel = Color(0xFF141F33)
private val Muted = Color(0xFF9BAAC3)
private val Accent = Color(0xFF6D9EFF)

private data class TvApp(val name: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 13, 25)
        window.navigationBarColor = android.graphics.Color.rgb(8, 13, 25)
        setContent { SmartTvApp() }
    }
}

@Composable
private fun SmartTvApp() {
    val context = LocalContext.current
    val pm = context.packageManager
    var apps by remember { mutableStateOf(findLaunchableApps(pm)) }
    var search by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(loadFavorites(context)) }
    var tab by remember { mutableStateOf("Inicio") }
    var clock by remember { mutableStateOf(currentTime()) }
    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) { clock = currentTime(); delay(30_000) }
    }

    val visibleApps = apps.filter { app ->
        app.name.contains(search, ignoreCase = true) &&
            (tab != "Favoritos" || favorites.contains(app.packageName))
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Night, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        BoxWithConstraints(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(Color(0xFF080D19), Color(0xFF101D35), Color(0xFF090E1A)))
            )
        ) {
            val tvMode = maxWidth >= 600.dp
            val horizontalPadding = if (tvMode) 42.dp else 20.dp
            Column(Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = if (tvMode) 28.dp else 18.dp)) {
                Header(clock, tvMode, onRefresh = { apps = findLaunchableApps(pm) }, onSettings = { showInfo = true })
                Spacer(Modifier.height(if (tvMode) 28.dp else 18.dp))

                if (tvMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tabs(tab) { tab = it }
                        Spacer(Modifier.weight(1f))
                        SearchBox(search, { search = it }, Modifier.width(270.dp))
                    }
                } else {
                    Tabs(tab) { tab = it }
                    Spacer(Modifier.height(12.dp))
                    SearchBox(search, { search = it }, Modifier.fillMaxWidth())
                }

                if (tab == "Inicio" && search.isBlank()) {
                    Spacer(Modifier.height(if (tvMode) 24.dp else 18.dp))
                    HeroBanner(tvMode)
                }

                Spacer(Modifier.height(if (tvMode) 24.dp else 18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (tab == "Favoritos") "TUS FAVORITOS" else "TUS APLICACIONES", color = Muted,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${visibleApps.size} apps", color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.height(14.dp))

                if (visibleApps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (tab == "Favoritos") "Aún no tienes favoritos. Marca una estrella en una aplicación." else "No se encontraron aplicaciones compatibles.", color = Muted)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (tvMode) 150.dp else 104.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(if (tvMode) 18.dp else 10.dp),
                        verticalArrangement = Arrangement.spacedBy(if (tvMode) 18.dp else 10.dp)
                    ) {
                        items(visibleApps, key = { it.packageName }) { app ->
                            AppCard(
                                app = app,
                                tvMode = tvMode,
                                favorite = favorites.contains(app.packageName),
                                onToggleFavorite = {
                                    favorites = if (favorites.contains(app.packageName)) favorites - app.packageName else favorites + app.packageName
                                    saveFavorites(context, favorites)
                                },
                                onOpen = {
                                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                                        ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER).setPackage(app.packageName)
                                    try { context.startActivity(launchIntent) } catch (_: Exception) { }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("LYGANE Smart TV") },
            text = { Text("Launcher experimental para teléfonos Android y Android TV / Google TV. Para convertirlo en pantalla de inicio, selecciónalo como aplicación HOME cuando Android lo ofrezca. La lista depende de las aplicaciones que cada fabricante permita consultar.") },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Aceptar") } }
        )
    }
}

@Composable
private fun Header(clock: String, tvMode: Boolean, onRefresh: () -> Unit, onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("LYGANE  /  SMART TV", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.2.sp)
            Text("Tu entretenimiento, a tu manera", color = Color.White, fontSize = if (tvMode) 27.sp else 19.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(clock, color = Color.White, fontSize = if (tvMode) 27.sp else 19.sp, fontWeight = FontWeight.Light)
            if (tvMode) Text(SimpleDateFormat("EEEE, d MMMM", Locale("es", "PE")).format(Date()), color = Muted, fontSize = 11.sp)
        }
        IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White) }
        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Información", tint = Color.White) }
    }
}

@Composable
private fun Tabs(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Inicio", "Aplicaciones", "Favoritos").forEach { item ->
            val active = selected == item
            Surface(
                color = if (active) Color(0xFF315A9D) else Color(0xFF17243A),
                shape = RoundedCornerShape(50),
                modifier = Modifier.clickable { onSelect(item) }.focusable()
            ) { Text(item, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)) }
        }
    }
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = modifier.height(54.dp),
        singleLine = true, placeholder = { Text("Buscar aplicación", color = Muted, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF34425B), cursorColor = Accent
        )
    )
}

@Composable
private fun HeroBanner(tvMode: Boolean) {
    Row(
        Modifier.fillMaxWidth().height(if (tvMode) 158.dp else 122.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF294B82), Color(0xFF192A49), Color(0xFF442E68))))
            .padding(if (tvMode) 26.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("BIENVENIDO A TU SMART TV", color = Color(0xFFC2D5F7), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(7.dp))
            Text("Todo tu mundo\nde entretenimiento.", color = Color.White, fontSize = if (tvMode) 25.sp else 19.sp, lineHeight = if (tvMode) 29.sp else 22.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Default.Apps, null, tint = Color.White.copy(alpha = .16f), modifier = Modifier.size(if (tvMode) 94.dp else 65.dp))
    }
}

@Composable
private fun AppCard(app: TvApp, tvMode: Boolean, favorite: Boolean, onToggleFavorite: () -> Unit, onOpen: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().height(if (tvMode) 142.dp else 112.dp)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onOpen).focusable(),
        shape = RoundedCornerShape(20.dp),
        color = if (focused) Color(0xFF263F68) else Panel,
        border = if (focused) BorderStroke(2.dp, Accent) else null,
        tonalElevation = if (focused) 8.dp else 1.dp
    ) {
        Box(Modifier.fillMaxSize().padding(10.dp)) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                val bmp = remember(app.packageName) {
                    val b = android.graphics.Bitmap.createBitmap(144, 144, android.graphics.Bitmap.Config.ARGB_8888)
                    val c = android.graphics.Canvas(b)
                    app.icon.setBounds(0, 0, 144, 144)
                    app.icon.draw(c)
                    b.asImageBitmap()
                }
                androidx.compose.foundation.Image(bmp, null, Modifier.size(if (tvMode) 58.dp else 46.dp))
                Spacer(Modifier.height(8.dp))
                Text(app.name, color = Color.White, fontSize = if (tvMode) 13.sp else 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onToggleFavorite, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, "Favorito", tint = if (favorite) Color(0xFFFFD166) else Muted, modifier = Modifier.size(17.dp))
            }
        }
    }
}

private fun findLaunchableApps(pm: PackageManager): List<TvApp> {
    val intents = listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    )
    return intents.flatMap { intent ->
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL).mapNotNull { info ->
            try { TvApp(info.loadLabel(pm).toString(), info.activityInfo.packageName, info.loadIcon(pm)) }
            catch (_: Exception) { null }
        }
    }.distinctBy { it.packageName }.sortedBy { it.name.lowercase(Locale.getDefault()) }
}

private fun currentTime() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
private fun loadFavorites(context: android.content.Context): Set<String> =
    context.getSharedPreferences("smart_tv", 0).getStringSet("favorites", emptySet())?.toSet() ?: emptySet()
private fun saveFavorites(context: android.content.Context, favorites: Set<String>) {
    context.getSharedPreferences("smart_tv", 0).edit().putStringSet("favorites", favorites).apply()
}
