package com.youtubecc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.youtubecc.app.data.ChannelInfo
import com.youtubecc.app.data.VideoInfo
import com.youtubecc.app.data.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as YouTubeCCApplication
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(app.container)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(container: AppContainer) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("تحليل قناة", "الرائج", "بحث", "إعدادات")
    Scaffold(
        topBar = { TopAppBar(title = { Text("YouTube Control Center") }) },
        bottomBar = {
            NavigationBar {
                listOf(
                    Icons.Default.Analytics to titles[0],
                    Icons.Default.Whatshot to titles[1],
                    Icons.Default.Search to titles[2],
                    Icons.Default.Settings to titles[3]
                ).forEachIndexed { i, (icon, label) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> AnalyzeScreen(container)
                1 -> TrendingScreen(container)
                2 -> SearchScreen(container)
                3 -> SettingsScreen(container)
            }
        }
    }
}

class AnalyzeVm(private val repo: YouTubeRepository) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _channel = MutableStateFlow<ChannelInfo?>(null)
    val channel: StateFlow<ChannelInfo?> = _channel.asStateFlow()
    private val _videos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val videos: StateFlow<List<VideoInfo>> = _videos.asStateFlow()

    fun analyze(input: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val ch = repo.resolveChannel(input)
                _channel.value = ch
                _videos.value = repo.channelVideos(ch.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "خطأ غير معروف"
                _channel.value = null
                _videos.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}

class ListVm(private val repo: YouTubeRepository) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _videos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val videos: StateFlow<List<VideoInfo>> = _videos.asStateFlow()

    fun loadTrending(region: String = "EG") {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _videos.value = repo.trending(region)
            } catch (e: Exception) {
                _error.value = e.message ?: "خطأ"
                _videos.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(q: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _videos.value = repo.searchVideos(q)
            } catch (e: Exception) {
                _error.value = e.message ?: "خطأ"
                _videos.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}

fun vmFactory(container: AppContainer) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AnalyzeVm::class.java) ->
                AnalyzeVm(container.repository) as T
            modelClass.isAssignableFrom(ListVm::class.java) ->
                ListVm(container.repository) as T
            else -> throw IllegalArgumentException("Unknown VM")
        }
    }
}

fun fmt(n: Long): String {
    if (n < 0) return "مخفي"
    return NumberFormat.getNumberInstance(Locale("ar")).format(n)
}

@Composable
fun AnalyzeScreen(container: AppContainer) {
    val vm: AnalyzeVm = viewModel(factory = vmFactory(container))
    var input by remember { mutableStateOf("") }
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val channel by vm.channel.collectAsState()
    val videos by vm.videos.collectAsState()

    Column(Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("رابط / @handle / Channel ID") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { if (input.isNotBlank()) vm.analyze(input) },
            enabled = !loading && input.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "جاري التحليل..." else "تحليل القناة")
        }
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        channel?.let { ch ->
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (ch.thumbnail.isNotEmpty()) {
                            AsyncImage(
                                model = ch.thumbnail,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Column {
                            Text(ch.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (ch.handle.isNotEmpty()) Text(ch.handle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Stat("مشتركين", fmt(ch.subscribers))
                        Stat("مشاهدات", fmt(ch.views))
                        Stat("فيديوهات", fmt(ch.videos))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("أحدث الفيديوهات", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(videos, key = { it.id }) { v -> VideoRow(v) }
            }
        }
    }
}

@Composable
fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun VideoRow(v: VideoInfo) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (v.thumbnail.isNotEmpty()) {
                AsyncImage(
                    model = v.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.size(width = 96.dp, height = 54.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(v.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text("${fmt(v.views)} مشاهدة · ${v.channelTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun TrendingScreen(container: AppContainer) {
    val vm: ListVm = viewModel(factory = vmFactory(container))
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val videos by vm.videos.collectAsState()
    LaunchedEffect(Unit) { if (videos.isEmpty()) vm.loadTrending() }
    Column(Modifier.padding(16.dp).fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("الفيديوهات الرائجة (مصر)", fontWeight = FontWeight.Bold)
            TextButton(onClick = { vm.loadTrending() }, enabled = !loading) { Text("تحديث") }
        }
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
        if (loading) Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(videos, key = { it.id }) { VideoRow(it) }
        }
    }
}

@Composable
fun SearchScreen(container: AppContainer) {
    val vm: ListVm = viewModel(factory = vmFactory(container))
    var q by remember { mutableStateOf("") }
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val videos by vm.videos.collectAsState()
    Column(Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(
            value = q, onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("كلمة البحث") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { if (q.isNotBlank()) vm.search(q) },
            enabled = !loading && q.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (loading) "جاري البحث..." else "بحث") }
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(videos, key = { it.id }) { VideoRow(it) }
        }
    }
}

@Composable
fun SettingsScreen(container: AppContainer) {
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var channelId by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        apiKey = container.preferences.getApiKey()
        channelId = container.preferences.getChannelId()
    }
    Column(Modifier.padding(16.dp).fillMaxSize()) {
        Text("الإعدادات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it; saved = false },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("مفتاح YouTube Data API v3") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = channelId,
            onValueChange = { channelId = it; saved = false },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("معرّف قناتك (اختياري)") },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    container.preferences.setApiKey(apiKey)
                    container.preferences.setChannelId(channelId)
                    saved = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("حفظ") }
        if (saved) Text("تم الحفظ", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            "احصل على المفتاح من Google Cloud Console وفعّل YouTube Data API v3. البيانات المعروضة عامة فقط.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
