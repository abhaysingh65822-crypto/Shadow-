package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlannerTaskEntity
import com.example.ui.theme.*

data class TaskCategoryMetric(
    val type: String,
    val displayName: String,
    val completedCount: Int,
    val totalCount: Int,
    val color: Color
) {
    val percentage: Int
        get() = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0
}

@Composable
fun TaskCompletionChartComponent(
    tasks: List<PlannerTaskEntity>,
    onOpenPlanner: () -> Unit,
    onGenerateAiPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val overallPercentage = if (totalTasks > 0) {
        ((completedTasks.toFloat() / totalTasks) * 100).toInt()
    } else 0

    val animatedSweep by animateFloatAsState(
        targetValue = (overallPercentage / 100f) * 360f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "sweep_animation"
    )

    // Category breakdown
    val categoryMetrics = remember(tasks) {
        val groups = tasks.groupBy { it.taskType.uppercase() }
        listOf(
            TaskCategoryMetric(
                type = "STUDY",
                displayName = "Study Sessions",
                completedCount = groups["STUDY"]?.count { it.isCompleted } ?: 0,
                totalCount = groups["STUDY"]?.size ?: 0,
                color = ForgeIndigoLight
            ),
            TaskCategoryMetric(
                type = "REVISION",
                displayName = "Spaced Revision",
                completedCount = groups["REVISION"]?.count { it.isCompleted } ?: 0,
                totalCount = groups["REVISION"]?.size ?: 0,
                color = ForgeAmber
            ),
            TaskCategoryMetric(
                type = "PRACTICE",
                displayName = "Problem Sets",
                completedCount = groups["PRACTICE"]?.count { it.isCompleted } ?: 0,
                totalCount = groups["PRACTICE"]?.size ?: 0,
                color = ForgeEmerald
            ),
            TaskCategoryMetric(
                type = "TEST",
                displayName = "Mock Tests",
                completedCount = groups["TEST"]?.count { it.isCompleted } ?: 0,
                totalCount = groups["TEST"]?.size ?: 0,
                color = ForgeRose
            )
        ).filter { it.totalCount > 0 }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_completion_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForgeEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DonutLarge,
                                contentDescription = "Task Chart",
                                tint = ForgeEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Task Completion Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Execution rate & category distribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (overallPercentage >= 70) ForgeEmerald.copy(alpha = 0.15f) else ForgeAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (overallPercentage >= 70) ForgeEmerald.copy(alpha = 0.4f) else ForgeAmber.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "$overallPercentage% Done",
                        color = if (overallPercentage >= 70) ForgeEmerald else ForgeAmber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                // Empty state
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No study tasks scheduled yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onGenerateAiPlan,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Study Plan", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Recharts-inspired Donut Visualizer + Stats Column
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart Canvas
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        val primaryGradient = Brush.sweepGradient(
                            colors = listOf(
                                ForgeCyan,
                                ForgeIndigoLight,
                                ForgeEmerald,
                                ForgeCyan
                            )
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            val centerOffset = Offset(size.width / 2, size.height / 2)
                            val topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                            val arcSize = Size(radius * 2, radius * 2)

                            // Track Arc
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Progress Arc
                            if (animatedSweep > 0f) {
                                drawArc(
                                    brush = primaryGradient,
                                    startAngle = -90f,
                                    sweepAngle = animatedSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Center Metric Label
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallPercentage%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "$completedTasks/$totalTasks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Summary Stats Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Completed:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$completedTasks tasks",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ForgeEmerald
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pending:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${totalTasks - completedTasks} tasks",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ForgeAmber
                            )
                        }

                        val totalPlannedMinutes = tasks.sumOf { it.durationMinutes }
                        val completedMinutes = tasks.filter { it.isCompleted }.sumOf { it.durationMinutes }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Study Time:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${completedMinutes}m / ${totalPlannedMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Category Progress Bars Breakdown (Recharts-style multi-metric layout)
                if (categoryMetrics.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Breakdown by Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    categoryMetrics.forEach { cat ->
                        Column(modifier = Modifier.padding(vertical = 3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(cat.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "${cat.completedCount}/${cat.totalCount} (${cat.percentage}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = cat.color,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { if (cat.totalCount > 0) cat.completedCount.toFloat() / cat.totalCount else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = cat.color,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Link to Smart Planner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenPlanner)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage & Reorder in Smart Planner",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForgeIndigoLight,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = ForgeIndigoLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
