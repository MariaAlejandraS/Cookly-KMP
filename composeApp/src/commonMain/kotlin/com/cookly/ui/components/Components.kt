package com.cookly.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cookly.data.model.Dates
import com.cookly.data.model.ExpiryStatus
import com.cookly.data.model.FoodCategory
import com.cookly.ui.theme.CooklyColors
import com.cookly.ui.theme.LocalCookly

/** Bloque de marca: ícono de nevera, COOKLY y el lema. Se usa en login, registro y Acerca de. */
@Composable
fun BrandHeader(subtitle: String = "Tu nevera digital", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CooklyColors.brandGradient)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .shadow(6.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(LocalCookly.current.surface)
                .padding(5.dp),
        ) {
            Box(
                Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(CooklyColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Kitchen, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        Text("COOKLY", color = CooklyColors.OnGradient, fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(
            subtitle,
            color = CooklyColors.OnGradient.copy(alpha = 0.75f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Tarjeta blanca con bordes redondeados: el contenedor de casi todas las secciones. */
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalCookly.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

/** Botón principal: cápsula con el degradado de marca y texto #111111 (igual que PrimaryButtonStyle de iOS). */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(CircleShape)
            .background(CooklyColors.brandGradient)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = CooklyColors.OnGradient)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = CooklyColors.OnGradient, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/** Botón secundario: cápsula blanca con borde verde azulado. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, contentColor: Color? = null) {
    val palette = LocalCookly.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(palette.surface)
            .border(1.5.dp, CooklyColors.Primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = contentColor ?: palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/** Campo de texto con su mensaje de error debajo. Recibe el valor y avisa los cambios (state hoisting). */
@Composable
fun CooklyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
) {
    val leading: (@Composable () -> Unit)? =
        leadingIcon?.let { icon -> @Composable { Icon(icon, contentDescription = null) } }
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            leadingIcon = leading,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        ErrorText(error)
    }
}

/** Campo de contraseña con el ojito para mostrarla u ocultarla. El ojito es estado local de la vista. */
@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String = "Contraseña", error: String? = null) {
    var visible by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = CooklyColors.Primary,
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        ErrorText(error)
    }
}

@Composable
fun ErrorText(message: String?) {
    if (message != null) {
        Text(
            message,
            color = LocalCookly.current.danger,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
    }
}

/** Ícono cuadrado suave (se usa en filas y tarjetas). */
@Composable
fun IconTile(icon: ImageVector, modifier: Modifier = Modifier, size: Int = 48) {
    Box(
        modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CooklyColors.Primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = LocalCookly.current.brandForeground, modifier = Modifier.size((size / 2).dp))
    }
}

/**
 * Foto del producto descargada de internet con Coil (multiplataforma).
 * Mientras carga, o si no hay foto, se ve el ícono de respaldo que queda detrás.
 */
@Composable
fun ProductImage(url: String?, fallback: ImageVector, modifier: Modifier = Modifier, size: Int = 48) {
    Box(
        modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CooklyColors.Primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(fallback, contentDescription = null, tint = LocalCookly.current.brandForeground, modifier = Modifier.size((size / 2).dp))
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(LocalCookly.current.surface),
            )
        }
    }
}

/** Etiqueta de color según el estado de vencimiento: rojo, amarillo o verde. */
@Composable
fun StatusPill(daysLeft: Int, status: ExpiryStatus) {
    val p = LocalCookly.current
    val (bg, fg) = when (status) {
        ExpiryStatus.EXPIRED -> p.dangerSoft to p.danger
        ExpiryStatus.SOON -> p.warningSoft to p.warning
        ExpiryStatus.FRESH -> p.successSoft to p.success
    }
    Pill(Dates.expiryLabel(daysLeft), bg, fg)
}

@Composable
fun Pill(text: String, background: Color, foreground: Color) {
    Text(
        text,
        color = foreground,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Fondo de un elemento seleccionado: degradado de marca; si no está seleccionado, la superficie. */
@Composable
private fun Modifier.brandSelected(selected: Boolean, shape: androidx.compose.ui.graphics.Shape): Modifier {
    val p = LocalCookly.current
    return if (selected) this.clip(shape).background(CooklyColors.brandGradient)
    else this.clip(shape).background(p.surface).border(1.dp, MaterialTheme.colorScheme.outline, shape)
}

/**
 * Barra de opciones (filtros, Una vez / Se repite). No guarda estado propio:
 * recibe la opción elegida y avisa cuando cambia. Es el equivalente al @Binding de SwiftUI.
 */
@Composable
fun <T> SegmentedChips(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .brandSelected(isSelected, RoundedCornerShape(22.dp))
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(option),
                    color = if (isSelected) CooklyColors.OnGradient else LocalCookly.current.ink,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/** Chip pequeño seleccionable (horarios sugeridos, días de la semana). */
@Composable
fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(36.dp)
            .brandSelected(selected, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (selected) CooklyColors.OnGradient else LocalCookly.current.ink,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Ícono de cada categoría (se decide en la UI, el modelo no sabe de íconos). */
fun FoodCategory.icon(): ImageVector = when (this) {
    FoodCategory.CARNE_MOLIDA -> Icons.Filled.LunchDining
    FoodCategory.CORTES_CARNE -> Icons.Filled.DinnerDining
    FoodCategory.POLLO -> Icons.Filled.Egg
    FoodCategory.PESCADO -> Icons.Filled.SetMeal
    FoodCategory.COCIDA -> Icons.Filled.RiceBowl
    FoodCategory.EMBUTIDOS -> Icons.Filled.Tapas
    FoodCategory.LACTEOS -> Icons.Filled.LocalDrink
    FoodCategory.VERDURAS_HOJA -> Icons.Filled.Eco
    FoodCategory.FRUTAS_BLANDAS -> Icons.Filled.Spa
    FoodCategory.FRUTAS_FIRMES -> Icons.Filled.Park
    FoodCategory.OTROS -> Icons.Filled.ShoppingBasket
}

/** Cuadrícula de categorías, 3 por fila. La elegida va con el degradado de marca. */
@Composable
fun CategoryGrid(selected: FoodCategory, onSelect: (FoodCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FoodCategory.entries.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { category ->
                    val isSelected = category == selected
                    val shape = RoundedCornerShape(16.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .height(104.dp)
                            .clip(shape)
                            .background(if (isSelected) CooklyColors.brandGradient else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.surfaceVariant))
                            .then(if (isSelected) Modifier.border(2.dp, CooklyColors.Primary, shape) else Modifier)
                            .clickable { onSelect(category) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val tint = if (isSelected) CooklyColors.OnGradient else LocalCookly.current.ink
                        Icon(category.icon(), contentDescription = null, tint = tint)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            category.label,
                            color = tint,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Contador con botones − y + (cantidad, días de aviso). */
@Composable
fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, range: IntRange) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = LocalCookly.current.ink)
        StepButton(Icons.Filled.Remove, "Menos", enabled = value > range.first) { onChange(value - 1) }
        Spacer(Modifier.width(10.dp))
        StepButton(Icons.Filled.Add, "Más", enabled = value < range.last) { onChange(value + 1) }
    }
}

@Composable
private fun StepButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(LocalCookly.current.surface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (enabled) CooklyColors.Primary else MaterialTheme.colorScheme.outline,
        )
    }
}

/** Círculo con las iniciales del nombre. */
@Composable
fun Avatar(name: String, size: Int = 56) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(CooklyColors.brandGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials.ifEmpty { "?" }, color = CooklyColors.OnGradient, fontWeight = FontWeight.Bold, fontSize = (size / 2.6).sp)
    }
}

/** Estado vacío con círculos concéntricos (igual a IllustratedEmpty de iOS). */
@Composable
fun IllustratedEmpty(
    icon: ImageVector,
    title: String,
    message: String,
    primaryTitle: String? = null,
    onPrimary: (() -> Unit)? = null,
    secondaryTitle: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(132.dp).border(10.dp, CooklyColors.Secondary.copy(alpha = 0.45f), CircleShape))
            Box(Modifier.size(92.dp).border(10.dp, CooklyColors.Primary.copy(alpha = 0.7f), CircleShape))
            Box(Modifier.size(64.dp).clip(CircleShape).background(CooklyColors.brandGradient), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = CooklyColors.OnGradient)
            }
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LocalCookly.current.ink)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (primaryTitle != null && onPrimary != null) PrimaryButton(primaryTitle, onClick = onPrimary)
        if (secondaryTitle != null && onSecondary != null) SecondaryButton(secondaryTitle, onClick = onSecondary)
    }
}

/** Tarjeta de información con título (categoría, ingredientes, alérgenos). */
@Composable
fun InfoCard(title: String, text: String) {
    SectionCard {
        Text(title, fontWeight = FontWeight.Bold, color = LocalCookly.current.ink)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Aviso flotante "Alimento agregado a tu nevera": cápsula oscura con un chulo.
 * Aparece abajo y se esconde solo a los 2 segundos.
 */
@Composable
fun CooklyToast(message: String?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2_000)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier,
    ) {
        Row(
            Modifier
                .heightIn(min = 44.dp)
                .clip(CircleShape)
                .background(CooklyColors.ToastBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = CooklyColors.Secondary)
            Spacer(Modifier.width(8.dp))
            Text(message.orEmpty(), color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Insignia de Nutri-Score con su color oficial (A verde ... E rojo). */
@Composable
fun NutriScoreBadge(grade: String) {
    val color = when (grade.lowercase()) {
        "a" -> Color(0xFF038141)
        "b" -> Color(0xFF85BB2F)
        "c" -> Color(0xFFFECB02)
        "d" -> Color(0xFFEE8100)
        else -> Color(0xFFE63E11)
    }
    Pill("Nutri-Score ${grade.uppercase()}", color, if (grade.lowercase() == "c") Color(0xFF111111) else Color.White)
}
