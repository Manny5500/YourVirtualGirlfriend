package com.mavapps.yvg

import android.os.Bundle

import com.mavapps.yvg.utils.AITypes
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.mavapps.yvg.ui.theme.YVGTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mavapps.yvg.model.Chat
import com.mavapps.yvg.utils.ChatType
import com.mavapps.yvg.utils.theme1
import com.mavapps.yvg.utils.theme2
import com.mavapps.yvg.utils.theme3
import com.mavapps.yvg.utils.theme4
import com.mavapps.yvg.utils.theme6
import kotlinx.coroutines.delay

val primaryColor = Color(0xFFB3304C)
val secondaryColor = Color(0xFFEF95A3)
val tertiaryColor = Color(0xFFFFD6DC)

var themeColor : Colors = theme1

//if 0 model 1 user
/*
data class Chat(
    val user: Int = 0,
    val list: List<String> = emptyList(),
    val chatWith: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

 */


data class Colors(
    val primary : Color = Color(0xFFB3304C),
    val secondary : Color = Color(0xFFEF95A3),
    val tertiary : Color = Color(0xFFFFD6DC)
)



class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_db"
        ).build()
        val dao = db.chatDao()
        val viewModel: MainViewModel = MainViewModel(dao)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            themeColor = when(uiState.gf.aiId){
                1 -> theme2
                2 -> theme3
                3 -> theme4
                5 -> theme6
                else -> theme1
            }
            val isOnSplash = remember { mutableStateOf(true) }
            YVGTheme {
                if(isOnSplash.value){
                    SplashScreen {
                        isOnSplash.value = false
                    }
                }else{
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if(uiState.currentScreen == "chat"){
                                TopAppBar(
                                    title = {
                                        Row{
                                            AsyncImage(
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .width(40.dp)
                                                    .clip(CircleShape)
                                                    .clickable { },
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(uiState.gf.imageResource)
                                                    .placeholder(R.drawable.user)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                            )

                                            Spacer(Modifier.width(20.dp))
                                            Column{
                                                Text(
                                                    text = uiState.gf.aiName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Active Now",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            viewModel.onBackButton("selection")
                                        }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Menu", tint = Color.White)
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = themeColor.primary)
                                )
                            }

                        }
                    ) { innerPadding ->
                        if(uiState.currentScreen == "selection"){
                            Selection(
                                onItemClicked = {
                                    viewModel.updateGF(it)
                                    viewModel.updateCurrentScreen("chat")
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }else{
                            LaunchedEffect(uiState.gf.aiId) {
                                viewModel.observeChats(uiState.gf.aiId, 1)
                            }
                            val chatList by viewModel.chats.collectAsState()
                            Greeting(
                                name = "Android",
                                modifier = Modifier.padding(innerPadding),
                                onValueChange = {viewModel.onValueChange(it)},
                                textFieldContent = uiState.userChat,
                                onSubmit = {
                                    viewModel.onUserSubmit()
                                },
                                list = chatList,
                                onDelete = {viewModel.onDeleteChat(it)},
                                imageResource = uiState.gf.imageResource
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun SelectionPreview(){
    YVGTheme {
        Selection({})
    }
}

 */

@Composable
fun Selection(
    onItemClicked : (Int) -> Unit,
    modifier: Modifier = Modifier
){
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState),
    ) {

        Text("Choose GF", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp))
        for((i, gf) in AITypes.girlfriendTypes.withIndex()){
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth().clickable { onItemClicked(i) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .clickable { },
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(gf.imageResource)
                        .placeholder(R.drawable.user)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Text(gf.aiName)
            }
            Spacer(Modifier.height(0.5.dp))
        }
    }
}



@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    textFieldContent: String,
    onSubmit: () -> Unit,
    list: List<Chat>,
    onDelete: (Int) -> Unit,
    imageResource: Int
) {
    val listState = rememberLazyListState()
    LaunchedEffect(list.size) {
        if(list.isNotEmpty()) listState.animateScrollToItem(list.size - 1)
    }
    Column(
        modifier = modifier
    ){

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(list) { chat ->
                if(chat.chatType == ChatType.AITYPE.id){
                    ModelChat(
                        message = chat.message,
                        imageResource = imageResource
                    )
                } else {
                    UserChat(
                        message =chat.message,
                        onDelete = {onDelete(chat.chatId)}
                    )
                }

            }
        }

        Box (
            modifier = Modifier.padding(20.dp)
        ){
            TextField(
                hint = "",
                textFieldHeight = 80.0,
                textFieldContent = textFieldContent,
                onValueChange = {onValueChange(it)},
                onSubmit = {onSubmit()}
            )
        }
    }
}


@Composable
fun TextField(
    hint: String,
    textFieldHeight: Double,
    textFieldContent: String,
    onValueChange:(String) -> Unit,
    onSubmit : ()-> Unit
) {
    Column (
        modifier = Modifier.fillMaxWidth()
    ){
        val color = themeColor.secondary
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight.dp),
            value = textFieldContent,
            onValueChange = {
                onValueChange(it)
            },
            placeholder = { Text(text = hint) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                unfocusedBorderColor = color
            ),
            shape = roundedShape,
            trailingIcon ={
                IconButton(onClick = { onSubmit(

                ) },
                    modifier = Modifier
                        .size(25.dp)) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Assist",
                        tint = themeColor.primary
                    )
                }
            }
        )
    }
}

@Composable
fun ModelChat(
    message: String,
    imageResource : Int = R.drawable.user
){
    Row{
        Spacer(Modifier.width(20.dp))
        AsyncImage(
            modifier = Modifier
                .height(40.dp)
                .width(40.dp)
                .clip(CircleShape)
                .clickable { },
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageResource)
                .placeholder(imageResource)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(20.dp))
        Column(
            Modifier.fillMaxWidth()
                .padding(end = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(themeColor.tertiary, shape = RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleSmall,
                    color = themeColor.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserChat(
    message : String,
    onDelete : () -> Unit
){
    var expanded by remember{mutableStateOf(false)}
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }

    Row{
        Spacer(Modifier.width(20.dp))
        Box{
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalAlignment = Alignment.End
            ) {

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(themeColor.secondary, shape = RoundedCornerShape(16.dp))
                        .padding(12.dp)
                        .pointerInput(Unit){
                            detectTapGestures {offset->
                                pressOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                                expanded = true
                            }
                        }
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }
            if(expanded){
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().background(Color.Red)
                ){
                    Spacer(modifier = Modifier.weight(1f).background(Color.Red))
                    DropdownMenu(
                        modifier = Modifier.align(Alignment.End),
                        expanded = expanded,
                        onDismissRequest = {expanded = false},
                        offset = pressOffset
                    ){
                        DropdownMenuItem(
                            text = {Text("Delete")},
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
val roundedShape = RoundedCornerShape(8.dp)

@Composable
fun SplashScreen(navToHome: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        navToHome()
    }
    Box(
        modifier =
            Modifier
            .fillMaxSize()
            .background(Color(0xFFB3304C)),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            modifier = Modifier
                .height(300.dp)
                .width(300.dp)
                .clip(CircleShape)
                .clickable { },
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.yvg_splashscreen)
                .placeholder(R.drawable.yvg_splashscreen)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

