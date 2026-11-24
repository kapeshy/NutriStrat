package com.example.nutristrat.ui.scanner

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScannerScreen(
    onPhotoCaptured:(Bitmap) -> Unit={},
    onGalleryImageSelected:(Uri)->Unit={},
    onSimulateScanClick: () -> Unit ={}
){
    //1.Camera Launcher:opens device camera and returns a Bitmap?(nullable if cancelled)
    val cameraLauncher=rememberLauncherForActivityResult(
        contract=ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        // Null-safe execution:only proceeds if the user actually snapped a photo
        bitmap?.let { capturedPhoto ->
            onPhotoCaptured(capturedPhoto)
        }
    }
    // 2. Gallery Launcher:opens device photo picker and returns a Uri?
    val galleryLauncher=rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){uri:Uri? ->
        uri?.let{selectedUri ->
            onGalleryImageSelected(selectedUri)
        }
    }
    Column(
        modifier= Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5EE))
            .padding(20.dp),
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        //Header
        Column(modifier=Modifier.fillMaxWidth()){
            Text(
                text="Scan your meal",
                fontSize=24.sp,
                fontWeight=FontWeight.Bold,
                color=Color(0xFF1B3B2B)
            )
            Spacer(modifier=Modifier.height(4.dp))
            Text(
                text="Fit your whole plate inside the frame",
                fontSize=13.sp,
                color=Color.Gray
            )
        }
        Spacer(modifier=Modifier.height(20.dp))
        //Dark Green AI Viewfinder Box("AI FOOD SCANNER . LIVE")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape=RoundedCornerShape(24.dp),
            colors=CardDefaults.cardColors(containerColor=Color(0xFF162B20))
        ){
            Box(
                modifier= Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ){
                //Top viewfinder status bar
                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically
                ){
                    Text(
                        text="AI FOOD SCANNER",
                        fontSize=11.sp,
                        color=Color(0xFF81C784),
                        letterSpacing=1.sp
                    )
                    Text(
                        text = "● LIVE",
                        fontSize=11.sp,
                        fontWeight=FontWeight.Bold,
                        color=Color(0xFFAED581)
                    )
                }
                //Center plate guide frame
                Surface(
                    modifier= Modifier
                        .size(210.dp)
                        .align(Alignment.Center)
                        .clickable{onSimulateScanClick()},
                    shape=RoundedCornerShape(20.dp),
                    color=Color(0xFF1E3A2B),
                    border=BorderStroke(2.dp,Color(0xFFAE581))
                ){
                    Column(
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.Center,
                        modifier=Modifier.padding(16.dp)
                    ){
                        Icon(
                            imageVector=Icons.Default.CameraAlt,
                            contentDescription="Plate  Frame",
                            tint=Color(0xFFAED581),
                            modifier=Modifier.size(40.dp)
                        )
                        Spacer(modifier=Modifier.height(8.dp))
                        Text(
                            text ="Tap shutter below",
                            fontSize=12.sp,
                            color=Color(0xFFC8E6C9)
                        )
                    }
                }
                //Bottom helper caption inside viewfinder
                Text(
                    text="Illuminate your food clearly",
                    fontSize=12.sp,
                    color=Color(0xFFB0BEC5),
                    modifier=Modifier.align(Alignment.BottomCenter)
                )
            }
        }
Spacer(modifier=Modifier.height(14.dp))
        Text(
            text="Your photo is used to estimate what's on your plate",
            fontSize=12.sp,
            color=Color.Gray
        )
        Spacer(modifier=Modifier.height(14.dp))

        //Circular Camera Shutter Button
        Surface(
            modifier= Modifier
                .size(68.dp)
                .clip(CircleShape)
                .clickable{cameraLauncher.launch(null)},
            shape=CircleShape,
            color=Color(0xFF2E6F40),
            border=BorderStroke(4.dp,Color(0xFFC8E6C9))
        ){
            Box(contentAlignment=Alignment.Center){
                Icon(
                    imageVector=Icons.Default.CameraAlt,
                    contentDescription="Take Photo",
                    tint=Color.White,
                    modifier=Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier=Modifier.height(12.dp))

        //Choose from gallery button
        TextButton(
            onClick={galleryLauncher.launch("image/*")}
        ){
            Icon(
                imageVector=Icons.Default.Image,
                contentDescription="Choose from gallery",
                tint=Color(0xFF2E6F40),
                modifier=Modifier.size(18.dp)
            )
            Spacer(modifier=Modifier.width(6.dp))
            Text(
                text="Choose from gallery",
                color=Color(0xFF2E6F40),
                fontWeight=FontWeight.SemiBold,
                fontSize=14.sp
            )
    }

    }
}