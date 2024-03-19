package com.adlr.wjbvisits.presentation.visits

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import com.adlr.wjbvisits.R
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VisitItem(
    context: Context,
    itemData: QueryDocumentSnapshot,
    navController: NavController,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    cutCornerSize: Dp = 30.dp,
) {
    val isCheckIn = itemData.getBoolean("isCheckIn")
    val isCheckOut = itemData.getBoolean("isCheckOut")
    val isSkip = itemData.getBoolean("isSkip")

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val clipPath = Path().apply {
                lineTo(size.width - cutCornerSize.toPx(), 0f)
                lineTo(size.width, cutCornerSize.toPx())
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            clipPath(clipPath) {
                val borderColor: Int = Color.Black.value.toInt()
                var color = Color(ContextCompat.getColor(context, R.color.grey_cat))
                val isDebt = itemData.getString("raport")?.lowercase()?.contains("menunggak")
                val firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")

                var isOrder = false
                val lastOrderDateString = itemData.getString("last_invoice")
                if (lastOrderDateString != null)
                {
                    val date: Date = dateFormat.parse(lastOrderDateString)
                    val lastOrderLocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    if (lastOrderLocalDate >= firstDayOfMonth)
                    {
                        isOrder = true
                    }
                }
                if (isDebt == true && !isOrder)
                {
                    color = Color(ContextCompat.getColor(context, R.color.bright_red_cat))
                }
                else if (isDebt != true && !isOrder)
                {
                    color = Color(ContextCompat.getColor(context, R.color.yellow_cat))
                }
                else if (isDebt == true && isOrder)
                {
                    color = Color(ContextCompat.getColor(context, R.color.blue_cat))
                }
                else if (isDebt != true && isOrder)
                {
                    color = Color(ContextCompat.getColor(context, R.color.grey_cat))
                }
                if (isCheckIn == true && isCheckOut == true) {
                    color = Color(ContextCompat.getColor(context, R.color.green_cat))
                } else if (isCheckIn == true) {
                    color = Color(ContextCompat.getColor(context, R.color.red_cat))
                }
                if (isSkip == true) {
                    color = Color(ContextCompat.getColor(context, R.color.skip_cat))
                }
                drawRoundRect(
                    color = color,
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
                drawRoundRect(
                    color = Color(
                        ColorUtils.blendARGB(borderColor, 0x000000, 0.2f)
                    ),
                    topLeft = Offset(size.width - cutCornerSize.toPx(), -100f),
                    size = Size(cutCornerSize.toPx() + 100f, cutCornerSize.toPx() + 100f),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(end = 32.dp)
        ) {
            itemData.getString("master_list_order")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            itemData.getString("daerah")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            itemData.getString("address")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            itemData.getString("pic")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
            itemData.getString("phone")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        IconButton(
            onClick = {
                navController.navigate("visit_details_screen/${itemData.id}")
            },
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Details",
                tint = MaterialTheme.colors.onSurface,
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}