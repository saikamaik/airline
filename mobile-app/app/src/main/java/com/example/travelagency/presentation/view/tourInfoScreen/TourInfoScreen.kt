package com.example.travelagency.presentation.view.tourInfoScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.travelagency.data.model.FlightModel
import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.TourModel
import com.example.travelagency.navigation.Screen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourInfoScreen(
    navHostController: NavHostController
) {
    val viewModel: TourInfoViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Информация о туре") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when (val tourResponse = uiState.value.tourResponse) {
            is Response.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Response.Success -> {
                TourInfoContent(
                    tour = tourResponse.data,
                    flightsResponse = uiState.value.flightsResponse,
                    isFavorite = uiState.value.isFavorite,
                    modifier = Modifier.padding(padding),
                    onBookClick = {
                        navHostController.navigate(
                            Screen.Request.route + "/${tourResponse.data.id}"
                        )
                    },
                    onFavoriteClick = {
                        viewModel.postUiEvent(com.example.travelagency.presentation.view.tourInfoScreen.uiEvent.TourInfoUiEvent.ToggleFavorite)
                    }
                )
            }

            is Response.Failure -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tourResponse.e ?: "Ошибка загрузки",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TourInfoContent(
    tour: TourModel,
    flightsResponse: Response<List<FlightModel>>,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onBookClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок с цветным фоном
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(getColorForTour(tour.id)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tour.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 3
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tour.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // Кнопка избранного
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = tour.destinationCity, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${tour.durationDays} дней", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Описание",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tour.description ?: "Описание отсутствует",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Flights section
            Text(
                text = "Авиарейсы",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (flightsResponse) {
                is Response.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }

                is Response.Success -> {
                    if (flightsResponse.data.isEmpty()) {
                        Text(
                            text = "Рейсы не привязаны",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        flightsResponse.data.forEach { flight ->
                            FlightCard(flight = flight)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                is Response.Failure -> {
                    Text(
                        text = "Ошибка загрузки рейсов",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price and book button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Стоимость",
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatPrice(tour.price.toDouble()),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(onClick = onBookClick) {
                        Text("Забронировать")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FlightCard(flight: FlightModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Flight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = flight.flightNo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${flight.departureAirportCode} → ${flight.arrivalAirportCode}",
                    fontSize = 14.sp
                )
                Text(
                    text = formatDateTime(flight.scheduledDeparture),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    return format.format(price)
}

private fun formatDateTime(dateTime: String): String {
    return try {
        dateTime.replace("T", " ").take(16)
    } catch (e: Exception) {
        dateTime
    }
}

// Генерируем уникальный цвет для каждого тура на основе его ID
private fun getColorForTour(tourId: Long): Color {
    val colors = listOf(
        Color(0xFF1ABC9C), // Turquoise
        Color(0xFF3498DB), // Blue
        Color(0xFF9B59B6), // Purple
        Color(0xFFE74C3C), // Red
        Color(0xFFF39C12), // Orange
        Color(0xFF16A085), // Dark Turquoise
        Color(0xFF27AE60), // Green
        Color(0xFF2C3E50), // Dark Blue
        Color(0xFF8E44AD), // Dark Purple
        Color(0xFFF4D03F), // Yellow
        Color(0xFFE67E22), // Dark Orange
        Color(0xFF2980B9), // Strong Blue
        Color(0xFF85C1E9), // Light Blue
        Color(0xFFEC7063), // Light Red
        Color(0xFF45B39D)  // Sea Green
    )
    return colors[(tourId % colors.size).toInt()]
}

