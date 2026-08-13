package com.ptahstudio.myapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptahstudio.myapp.data.local.AppDatabase
import com.ptahstudio.myapp.data.local.ProjectEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.projectDao()

    val projects: StateFlow<List<ProjectEntity>> = dao.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProject(title: String, description: String) {
        viewModelScope.launch {
            dao.insert(
                ProjectEntity(
                    title = title,
                    description = description,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            dao.delete(project)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ProjectViewModel(application)
        setContent {
            var currentTheme by remember { mutableStateOf(PtahTheme.CLASICO) }
            val themeColors = ThemeManager.themes[currentTheme] ?: ThemeManager.themes[PtahTheme.CLASICO]!!

            // Custom Application Theme Wrapper using Material 3 colorScheme mapping
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = themeColors.primary,
                    onPrimary = themeColors.onPrimary,
                    background = themeColors.background,
                    onBackground = themeColors.onBackground,
                    surface = themeColors.surface,
                    onSurface = themeColors.onSurface
                )
            ) {
                PtahHomeScreen(
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    themeColors = themeColors,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtahHomeScreen(
    currentTheme: PtahTheme,
    onThemeChange: (PtahTheme) -> Unit,
    themeColors: ThemeColors,
    viewModel: ProjectViewModel
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var selectedTab by remember { mutableStateOf(0) }
    var themeMenuExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val localProjects by viewModel.projects.collectAsState()

    // Function to fetch posts in a safe background context
    fun loadPosts() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                posts = NetworkService.fetchPosts()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Ocurrió un error inesperado al obtener los datos."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ptah Studio Suite",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColors.primary
                ),
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { loadPosts() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refrescar Feed",
                                tint = themeColors.onPrimary
                            )
                        }
                    }
                    
                    // Floating-style theme selection menu embedded in Top Bar for accessibility
                    Box {
                        IconButton(onClick = { themeMenuExpanded = !themeMenuExpanded }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Cambiar Tema",
                                tint = themeColors.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = { themeMenuExpanded = false },
                            modifier = Modifier.background(themeColors.surface)
                        ) {
                            PtahTheme.values().forEach { theme ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                            color = if (theme == currentTheme) themeColors.primary else themeColors.onSurface,
                                            fontWeight = if (theme == currentTheme) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onThemeChange(theme)
                                        themeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = themeColors.primary,
                    contentColor = themeColors.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Proyecto")
                }
            }
        },
        containerColor = themeColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row to navigate between the Online API Feed and Offline Local Storage (Room DB)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = themeColors.surface,
                contentColor = themeColors.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("API REST Feed", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Proyectos Locales (Room)", fontWeight = FontWeight.SemiBold) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeColors.background),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTab == 0) {
                    // TAB 0: API Feed Section
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = themeColors.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Cargando publicaciones...",
                                    color = themeColors.onBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                               )
                            }
                        }
                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    "Error: $errorMessage",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Button(
                                    onClick = { loadPosts() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeColors.primary,
                                        contentColor = themeColors.onPrimary
                                    )
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                            ) {
                                items(posts) { post ->
                                    PostCard(post = post, themeColors = themeColors)
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Room DB Projects Offline Section
                    if (localProjects.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "No hay proyectos locales guardados",
                                color = themeColors.onBackground.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Presiona el botón '+' para agregar uno nuevo.",
                                color = themeColors.onBackground.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                        ) {
                            items(localProjects) { project ->
                                ProjectCardItem(
                                    project = project,
                                    themeColors = themeColors,
                                    onDeleteClick = { viewModel.deleteProject(project) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Dialog to Add a Local Project
    if (showAddDialog) {
        var projectTitle by remember { mutableStateOf("") }
        var projectDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "Nuevo Proyecto Local",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Título") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            focusedLabelColor = themeColors.primary
                        )
                    )
                    OutlinedTextField(
                        value = projectDesc,
                        onValueChange = { projectDesc = it },
                        label = { Text("Descripción") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            focusedLabelColor = themeColors.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectTitle.isNotBlank() && projectDesc.isNotBlank()) {
                            viewModel.addProject(projectTitle, projectDesc)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary,
                        contentColor = themeColors.onPrimary
                    )
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = themeColors.primary)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = themeColors.surface
        )
    }
}

@Composable
fun ProjectCardItem(
    project: ProjectEntity,
    themeColors: ThemeColors,
    onDeleteClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { sdf.format(Date(project.updatedAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = project.description,
                    fontSize = 14.sp,
                    color = themeColors.onSurface,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Actualizado: $formattedDate",
                    fontSize = 11.sp,
                    color = themeColors.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Light
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar proyecto",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}
