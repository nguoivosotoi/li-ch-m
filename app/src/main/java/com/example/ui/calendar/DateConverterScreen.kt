package com.example.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.data.LunarDataStore
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateConverterScreen(onNavigateBack: () -> Unit) {
    var isSolarToLunar by remember { mutableStateOf(true) }
    var inputDay by remember { mutableStateOf("") }
    var inputMonth by remember { mutableStateOf("") }
    var inputYear by remember { mutableStateOf("") }
    var isLeapMonth by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi Ngày Âm Dương") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = if (isSolarToLunar) 0 else 1) {
                Tab(
                    selected = isSolarToLunar,
                    onClick = { isSolarToLunar = true; resultText = "" },
                    text = { Text("Dương sang Âm") }
                )
                Tab(
                    selected = !isSolarToLunar,
                    onClick = { isSolarToLunar = false; resultText = "" },
                    text = { Text("Âm sang Dương") }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedTextField(
                    value = inputDay,
                    onValueChange = { inputDay = it },
                    label = { Text("Ngày") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = inputMonth,
                    onValueChange = { inputMonth = it },
                    label = { Text("Tháng") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = inputYear,
                    onValueChange = { inputYear = it },
                    label = { Text("Năm") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
            
            if (!isSolarToLunar) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLeapMonth, onCheckedChange = { isLeapMonth = it })
                    Text("Tháng Nhuận")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val d = inputDay.toIntOrNull()
                    val m = inputMonth.toIntOrNull()
                    val y = inputYear.toIntOrNull()
                    if (d != null && m != null && y != null) {
                        if (isSolarToLunar) {
                            val lunarDate = LunarDataStore.getLunarDate(y, m, d)
                            if (lunarDate != null) {
                                val leapStr = if (lunarDate.isLeapMonth) " (Nhuận)" else ""
                                resultText = "Âm lịch: Ngày ${lunarDate.day}, Tháng ${lunarDate.month}$leapStr, Năm ${lunarDate.year}"
                            } else {
                                resultText = "Không có dữ liệu cho ngày này."
                            }
                        } else {
                            val solarDate = LunarDataStore.getSolarDate(y, m, d, isLeapMonth)
                            if (solarDate != null) {
                                val parts = solarDate.split("-")
                                resultText = "Dương lịch: Ngày ${parts[2]}, Tháng ${parts[1]}, Năm ${parts[0]}"
                            } else {
                                resultText = "Không có dữ liệu cho ngày này."
                            }
                        }
                    } else {
                        resultText = "Vui lòng nhập ngày hợp lệ."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chuyển đổi")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (resultText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = resultText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
