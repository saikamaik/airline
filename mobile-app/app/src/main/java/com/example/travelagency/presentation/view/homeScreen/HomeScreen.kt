package com.example.travelagency.presentation.view.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.travelagency.data.model.Response
import com.example.travelagency.navigation.Screen
import com.example.travelagency.presentation.view.homeScreen.components.TourCard
import com.example.travelagency.presentation.view.homeScreen.components.RecommendationCard
import com.example.travelagency.presentation.view.homeScreen.components.EmptyRecommendationsCard
import com.example.travelagency.presentation.view.homeScreen.uiEvent.HomeUiEvent

import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    paddingValues: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp)
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState()
    val recommendations = viewModel.recommendations.collectAsState()
    val isLoadingRecommendations = viewModel.isLoadingRecommendations.collectAsState()
    
    // Логирование для диагностики
    androidx.compose.runtime.LaunchedEffect(uiState.value.toursResponse) {
        Log.d("HomeScreen", "toursResponse changed: ${uiState.value.toursResponse::class.simpleName}")
        when (val response = uiState.value.toursResponse) {
            is Response.Success -> {
                Log.d("HomeScreen", "Tours loaded: ${uiState.value.tours.size} items")
            }
            is Response.Failure -> {
                Log.e("HomeScreen", "Tours loading failed: ${response.e}")
            }
            is Response.Loading -> {
                Log.d("HomeScreen", "Tours loading...")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // TopBar
        TopAppBar(
            title = {
                Text(
                    text = "TravelAgency",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.postUiEvent(HomeUiEvent.ToggleFilters) }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Фильтры",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Диалог фильтров
        if (uiState.value.showFilters) {
            FiltersDialog(
                minPrice = uiState.value.minPrice,
                maxPrice = uiState.value.maxPrice,
                onMinPriceChange = { viewModel.postUiEvent(HomeUiEvent.OnMinPriceChange(it)) },
                onMaxPriceChange = { viewModel.postUiEvent(HomeUiEvent.OnMaxPriceChange(it)) },
                onApply = { 
                    viewModel.postUiEvent(HomeUiEvent.ApplyFilters)
                    viewModel.postUiEvent(HomeUiEvent.ToggleFilters)
                },
                onClear = {
                    viewModel.postUiEvent(HomeUiEvent.ClearFilters)
                    viewModel.postUiEvent(HomeUiEvent.ToggleFilters)
                },
                onDismiss = { viewModel.postUiEvent(HomeUiEvent.ToggleFilters) }
            )
        }
        
            // Search bar
            OutlinedTextField(
                value = uiState.value.searchQuery,
                onValueChange = { viewModel.postUiEvent(HomeUiEvent.OnSearchQueryChange(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск туров по направлению...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = if (uiState.value.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.postUiEvent(HomeUiEvent.OnSearchQueryChange("")) }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                } else null,
                singleLine = true
            )

            // Секция рекомендаций
            if (!isLoadingRecommendations.value && recommendations.value.isNotEmpty()) {
                Text(
                    text = "Рекомендуем для вас",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recommendations.value.take(3).forEach { recommendation ->
                        RecommendationCard(
                            recommendation = recommendation,
                            onClick = {
                                navHostController.navigate(
                                    Screen.TourInfo.route + "/${recommendation.tourId}"
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Популярные туры",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val response = uiState.value.toursResponse) {
                is Response.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Response.Success -> {
                    val tours = uiState.value.tours
                    if (tours.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Туры не найдены",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        val listState = rememberLazyListState()
                        
                        // Автоматическая загрузка следующей страницы при прокрутке вниз
                        val shouldLoadMore = remember {
                            derivedStateOf {
                                val layoutInfo = listState.layoutInfo
                                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                                val totalItems = layoutInfo.totalItemsCount
                                
                                lastVisibleItem != null &&
                                lastVisibleItem.index >= totalItems - 3 &&
                                uiState.value.hasMore &&
                                !uiState.value.isLoadingMore
                            }
                        }
                        
                        LaunchedEffect(shouldLoadMore.value) {
                            if (shouldLoadMore.value) {
                                viewModel.postUiEvent(HomeUiEvent.LoadMoreTours)
                            }
                        }
                        
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(tours) { tour ->
                                TourCard(
                                    tour = tour,
                                    onClick = {
                                        navHostController.navigate(
                                            Screen.TourInfo.route + "/${tour.id}"
                                        )
                                    }
                                )
                            }
                            
                            // Индикатор загрузки следующей страницы
                            if (uiState.value.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }

                is Response.Failure -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ошибка загрузки",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = response.e ?: "Попробуйте позже",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
private fun FiltersDialog(
    minPrice: Double?,
    maxPrice: Double?,
    onMinPriceChange: (Double?) -> Unit,
    onMaxPriceChange: (Double?) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры поиска", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Минимальная цена
                OutlinedTextField(
                    value = minPrice?.toString() ?: "",
                    onValueChange = { value ->
                        onMinPriceChange(value.toDoubleOrNull())
                    },
                    label = { Text("Минимальная цена (₽)") },
                    placeholder = { Text("Например, 10000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Максимальная цена
                OutlinedTextField(
                    value = maxPrice?.toString() ?: "",
                    onValueChange = { value ->
                        onMaxPriceChange(value.toDoubleOrNull())
                    },
                    label = { Text("Максимальная цена (₽)") },
                    placeholder = { Text("Например, 100000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Информация о фильтрах
                if (minPrice != null || maxPrice != null) {
                    Text(
                        text = buildString {
                            append("Цена: ")
                            if (minPrice != null) append("от ${minPrice.toInt()}₽")
                            if (minPrice != null && maxPrice != null) append(" ")
                            if (maxPrice != null) append("до ${maxPrice.toInt()}₽")
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onApply) {
                Text("Применить")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClear) {
                    Text("Сбросить")
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}
