package com.example.ui.calendar

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LunarDate
import com.example.data.LunarDataStore
import com.example.data.LunarEvent
import com.example.ui.theme.LunarDim
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunarAppUI(viewModel: MainViewModel) {
    var showConverter by remember { mutableStateOf(false) }

    if (showConverter) {
        DateConverterScreen(onNavigateBack = { showConverter = false })
    } else {
        MainCalendarScreen(viewModel = viewModel, onNavigateToConverter = { showConverter = true })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalendarScreen(viewModel: MainViewModel, onNavigateToConverter: () -> Unit) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lịch Âm Dương", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                actions = {
                    IconButton(onClick = onNavigateToConverter) {
                        Icon(Icons.Default.DateRange, contentDescription = "Đổi Ngày")
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = "Thêm nhắc nhở") },
                text = { Text("Sự kiện mới", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            CalendarHeader(currentMonth, onPrev = { viewModel.prevMonth() }, onNext = { viewModel.nextMonth() })
            Spacer(modifier = Modifier.height(16.dp))
            CalendarGrid(currentMonth)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Sự kiện sắp tới", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            EventsList(events, onDelete = { viewModel.deleteEvent(it) })
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, lDay, lMonth, isRecurring ->
                viewModel.addEvent(name, lDay, lMonth, isRecurring)
                showAddDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun SettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val currentScale by viewModel.fontSizeMultiplier.collectAsStateWithLifecycle()
    var sliderValue by remember { mutableStateOf(currentScale) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Cài đặt ứng dụng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Kích thước chữ", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0.8f..1.5f,
                    steps = 6,
                    onValueChangeFinished = {
                        viewModel.setFontSizeMultiplier(sliderValue)
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nhỏ", fontSize = 12.sp)
                    Text("Tiêu chuẩn", fontSize = 14.sp)
                    Text("Lớn", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Đóng") }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(currentMonth: Calendar, onPrev: () -> Unit, onNext: () -> Unit) {
    val m = currentMonth.get(Calendar.MONTH) + 1
    val y = currentMonth.get(Calendar.YEAR)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tháng trước", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text("Tháng $m, $y", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Tháng sau", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun CalendarGrid(currentMonth: Calendar) {
    val cal = currentMonth.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    
    val todayCal = Calendar.getInstance()
    val isCurrentMonth = todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) + 1 == month
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    val daysOfWeek = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                daysOfWeek.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (index == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            var currentDay = 1
            for (row in 0..5) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0..6) {
                        if (row == 0 && col < firstDayOfWeek || currentDay > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val lunarDate = LunarDataStore.getLunarDate(year, month, currentDay)
                            val isToday = isCurrentMonth && currentDay == todayDay
                            CalendarCell(
                                solarDay = currentDay,
                                lunarDate = lunarDate,
                                isToday = isToday,
                                isWeekend = col == 0 || col == 6,
                                modifier = Modifier.weight(1f)
                            )
                            currentDay++
                        }
                    }
                }
                if (currentDay > daysInMonth) break
            }
        }
    }
}

@Composable
fun CalendarCell(solarDay: Int, lunarDate: LunarDate?, isToday: Boolean, isWeekend: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bgColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isToday) MaterialTheme.colorScheme.onPrimary else if (isWeekend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val lunarTextColor = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else com.example.ui.theme.LunarDim
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable {
                if (lunarDate != null) {
                    val leapStr = if(lunarDate.isLeapMonth) " (Nhuận)" else ""
                    Toast.makeText(context, "Âm lịch: ${lunarDate.day}/${lunarDate.month}$leapStr", Toast.LENGTH_SHORT).show()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = solarDay.toString(), 
                fontSize = 16.sp, 
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium, 
                color = textColor
            )
            if (lunarDate != null) {
                val lunarText = if (lunarDate.day == 1 || solarDay == 1) "${lunarDate.day}/${lunarDate.month}" else "${lunarDate.day}"
                Text(
                    text = lunarText, 
                    fontSize = 10.sp, 
                    color = lunarTextColor, 
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun EventsList(events: List<LunarEvent>, onDelete: (LunarEvent) -> Unit) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Chưa có sự kiện nào", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(events) { event ->
                EventItem(event, onDelete = { onDelete(event) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun EventItem(event: LunarEvent, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ngày ${event.lunarDay}/${event.lunarMonth} âm lịch ${if(event.isRecurring) "(Hàng năm)" else ""}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(onDismiss: () -> Unit, onSave: (String, Int, Int, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var lunarDay by remember { mutableStateOf("") }
    var lunarMonth by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Thêm Sự kiện Âm lịch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên sự kiện (VD: Giỗ ông, Sinh nhật)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = lunarDay,
                        onValueChange = { lunarDay = it.filter { it.isDigit() } },
                        label = { Text("Ngày") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lunarMonth,
                        onValueChange = { lunarMonth = it.filter { it.isDigit() } },
                        label = { Text("Tháng") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                    Text("Lặp lại hàng năm")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val d = lunarDay.toIntOrNull()
                            val m = lunarMonth.toIntOrNull()
                            if (name.isNotBlank() && d != null && m != null && d in 1..30 && m in 1..12) {
                                onSave(name, d, m, isRecurring)
                            }
                        }
                    ) {
                        Text("Lưu & Đồng bộ Lịch")
                    }
                }
            }
        }
    }
}
