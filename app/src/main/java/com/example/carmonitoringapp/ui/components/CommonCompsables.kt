package com.example.carmonitoringapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
fun CustomSummaryBox(
  text: String,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier
      .padding(16.dp)
      .clickable { expanded = !expanded }
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (expanded) "Hide Summary" else "Show Summary",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )
        Text(
          text = if (expanded) "▲" else "▼",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      AnimatedVisibility(visible = expanded) {
        Text(
          text = text,
          modifier = Modifier.padding(top = 12.dp),
          style = MaterialTheme.typography.bodyMedium
        )
      }
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
