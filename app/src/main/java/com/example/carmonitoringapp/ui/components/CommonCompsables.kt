package com.example.carmonitoringapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButton(
  text: String,
  onClick: () -> Unit,
  backgroundColor: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
    shape = RoundedCornerShape(10.dp),
    modifier = modifier
      .width(140.dp)
      .height(48.dp),
    elevation = ButtonDefaults.buttonElevation(6.dp)
  ) {
    Text(
      text = text,
      color = Color.White,
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp
    )
  }
}

@Composable
fun CustomSummaryBox(text: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 100.dp)
      .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
    shape = RoundedCornerShape(8.dp),
    tonalElevation = 2.dp
  ) {
    Box(
      modifier = Modifier
        .padding(12.dp)
    ) {
      Text(
        text = if (text.isNotEmpty()) text else "Summary will appear here...",
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
      )
    }
  }
}


@Preview
@Composable
fun PreviewCustomActionButton() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceEvenly
  ) {
    CustomButton(
      text = "Start",
      onClick = { /* Start logic */ },
      backgroundColor = Color(0xFF4CAF50) // green
    )
    Spacer(modifier = Modifier.height(16.dp))
    CustomButton(
      text = "Stop",
      onClick = { /* Stop logic */ },
      backgroundColor = Color(0xFFF44336) // red
    )
    Spacer(modifier = Modifier.height(16.dp))
    CustomSummaryBox("Hello World")
  }
}
