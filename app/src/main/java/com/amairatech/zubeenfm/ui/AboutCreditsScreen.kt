package com.amairatech.zubeenfm.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.amairatech.zubeenfm.ui.theme.GradientPurpleStart
import com.amairatech.zubeenfm.ui.theme.GradientIndigoEnd
import com.amairatech.zubeenfm.ui.theme.GlassWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhiteBorder
import com.amairatech.zubeenfm.ui.theme.GlassPurple
import com.amairatech.zubeenfm.ui.theme.GlassPurpleBorder
import com.amairatech.zubeenfm.ui.theme.TextMuted
import com.amairatech.zubeenfm.ui.theme.TextPrimary
import com.amairatech.zubeenfm.ui.theme.TextPureWhite

// Local purples to replace amber/gold
private val PurpleMain = Color(0xFF8B5CF6)
private val PurpleLight = Color(0xFFA78BFA)
private val IndigoDark = Color(0xFF1E1B4B)

data class DependencyAttribution(
    val name: String,
    val version: String,
    val purpose: String,
    val license: String,
    val copyright: String,
    val projectUrl: String,
    val licenseText: String
)

/**
 * Comprehensive About & Credits Screen for ZUBEEN FM.
 * Includes:
 * 1. ZUBEEN FM Brand & Identity
 * 2. Tribute & Dedication (Respectful Assamese dedication & non-affiliation disclaimer)
 * 3. Open Source Attribution (Detailed library audit: NewPipe Extractor, Compose, Kotlin, OkHttp, Jsoup, Rhino, etc.)
 * 4. Full License Viewer Dialog
 * 5. About Developer (Rocky Sir / Amaira Tech)
 * 6. Non-Commercial Policy
 * 7. Fact-Based Privacy Policy
 * 8. Network & Third-Party Services
 * 9. Copyright & Content Notice
 */
@Composable
fun AboutCreditsScreen() {
    var selectedLicenseDetails by remember { mutableStateOf<DependencyAttribution?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientPurpleStart, GradientIndigoEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. ZUBEEN FM Header & Branding
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(GlassPurple)
                    .border(2.dp, PurpleMain, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📻", fontSize = 34.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ZUBEEN FM",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = PurpleMain
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Version 1.0 (FOSS Release)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PurpleLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "98.6 MHz • Guwahati, Assam",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Tribute & Dedication Section
            AboutCard(
                title = "Tribute & Dedication",
                accentColor = PurpleMain
            ) {
                Text(
                    text = "🪔 শ্ৰদ্ধাঞ্জলী",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleMain
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“তোমাৰ সুৰে আমাক সদায় জীয়াই থকাৰ সাহস দিব, জুবিন দা।”",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    color = TextPureWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "অসমবাসীৰ প্ৰাণৰ শিল্পী, হৃদয়স্পৰ্শী সুৰৰ জনক জুবিন গাৰ্গৰ অমৰ সংগীত সাধনা আৰু সৃষ্টিৰাজিৰ প্ৰতি আমাৰ সশ্ৰদ্ধ প্ৰণাম। এই এপ্লিকেচন তেওঁৰ সংগীত অনুৰাগীসকলৰ বাবে উৎসৰ্গিত এক ব্যৱহাৰিক শ্ৰদ্ধাঞ্জলী।",
                    fontSize = 12.5.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GlassWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
                ) {
                    Text(
                        text = "ZUBEEN FM is an independent personal fan project and is not affiliated with or endorsed by Zubeen Garg's estate, family, record labels, music platforms, or other rights holders unless explicitly stated.",
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Open-Source Attribution Section (Inspected from Gradle dependency tree)
            AboutCard(
                title = "Open-Source Attribution",
                accentColor = Color(0xFF0288D1)
            ) {
                Text(
                    text = "ZUBEEN FM is constructed using robust open-source software libraries. Full attribution and licensing metadata are provided below in compliance with each project's distribution license.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                val attributions = remember { getDependencyAttributions() }

                attributions.forEachIndexed { index, item ->
                    AttributionItemCard(
                        attribution = item,
                        onViewLicense = { selectedLicenseDetails = item }
                    )
                    if (index < attributions.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = GlassWhiteBorder
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. About Developer Section
            AboutCard(
                title = "About Developer",
                accentColor = PurpleLight
            ) {
                InfoRow(label = "Developer", value = "Rocky Sir")
                InfoRow(label = "Project / Team", value = "Amaira Tech")
                InfoRow(label = "Application", value = "ZUBEEN FM")
                InfoRow(label = "Project Type", value = "Personal / Non-Commercial Fan Project")
                InfoRow(label = "Origin", value = "Guwahati, Assam, India")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Non-Commercial Policy Section
            AboutCard(
                title = "Non-Commercial Policy",
                accentColor = Color(0xFFC084FC)
            ) {
                Text(
                    text = "ZUBEEN FM is created strictly as a non-commercial tribute application with zero commercial monetization:",
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                BulletPoint(text = "No paid subscriptions or paywalls")
                BulletPoint(text = "No banner ads, interstitial ads, or audio advertisements")
                BulletPoint(text = "No in-app purchases or commercial licensing")
                BulletPoint(text = "No sale or monetization of user data")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "“Non-commercial use does not by itself grant copyright or streaming rights. Music and other copyrighted material remain the property of their respective rights holders.”",
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = PurpleLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Fact-Based Privacy Policy Section
            AboutCard(
                title = "Privacy Policy",
                accentColor = Color(0xFF4CAF50)
            ) {
                Text(
                    text = "We believe in strict, transparent user privacy:",
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                BulletPoint(text = "No Account Required: No registration, phone number, or login needed.")
                BulletPoint(text = "No Analytics or Tracking: Contains no analytics SDKs, advertising IDs, or tracking pixels.")
                BulletPoint(text = "Network Requests: Audio stream discovery connects to public third-party endpoints solely to retrieve song metadata and audio streams.")
                BulletPoint(text = "Local Media Storage: Scoped MediaStore audio access is used on-device only and never uploaded.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Network & Third-Party Services Section
            AboutCard(
                title = "Network & Third-Party Services",
                accentColor = Color(0xFFFF9800)
            ) {
                Text(
                    text = "Music discovery and playback stream extraction interact with configured public third-party services.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“ZUBEEN FM does not claim ownership of third-party music recordings or catalogues.”",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurpleMain
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Copyright & Content Notice Section
            AboutCard(
                title = "Copyright & Content Notice",
                accentColor = Color(0xFFE91E63)
            ) {
                Text(
                    text = "All music tracks, audio master recordings, lyrics, and associated cover arts remain the sole property and copyright of their respective artists, composers, record labels, and publishers.\n\nZUBEEN FM is an independent fan tribute providing a synchronized radio experience for Assamese culture enthusiasts.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // 9. Full License Details Modal Dialog
        selectedLicenseDetails?.let { attr ->
            AlertDialog(
                onDismissRequest = { selectedLicenseDetails = null },
                containerColor = IndigoDark,
                title = {
                    Column {
                        Text(
                            text = attr.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleMain
                        )
                        Text(
                            text = "License: ${attr.license}",
                            fontSize = 12.sp,
                            color = PurpleLight
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Project URL: ${attr.projectUrl}\nCopyright: ${attr.copyright}\n\nFull License Terms:\n",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = attr.licenseText,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp,
                            color = TextMuted
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedLicenseDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
                    ) {
                        Text("Close", color = TextPureWhite)
                    }
                }
            )
        }
    }
}

@Composable
private fun AboutCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AttributionItemCard(
    attribution: DependencyAttribution,
    onViewLicense: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${attribution.name} (${attribution.version})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPureWhite,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = GlassPurple,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassPurpleBorder)
            ) {
                Text(
                    text = attribution.license,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = PurpleLight,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = attribution.purpose,
            fontSize = 11.5.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = attribution.copyright,
            fontSize = 10.5.sp,
            color = TextMuted.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "View Full License ›",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PurpleMain,
            modifier = Modifier
                .clickable { onViewLicense() }
                .padding(vertical = 2.dp)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextPureWhite
        )
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top
    ) {
        Text(text = "• ", fontSize = 12.sp, color = PurpleMain)
        Text(text = text, fontSize = 12.sp, lineHeight = 18.sp, color = TextPrimary)
    }
}

/**
 * Returns exact list of verified dependencies audited from Gradle build.
 */
private fun getDependencyAttributions(): List<DependencyAttribution> {
    return listOf(
        DependencyAttribution(
            name = "NewPipe Extractor",
            version = "v0.26.4",
            purpose = "Open-source multimedia stream resolution & audio URL deciphering",
            license = "GNU GPL v3.0+",
            copyright = "Copyright © 2018-2025 TeamNewPipe and contributors",
            projectUrl = "https://github.com/TeamNewPipe/NewPipeExtractor",
            licenseText = "GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007\n\nCopyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>\nEveryone is permitted to copy and distribute verbatim copies of this license document...\n\nZUBEEN FM uses NewPipeExtractor as a library and is NOT the official NewPipe project or affiliated with TeamNewPipe."
        ),
        DependencyAttribution(
            name = "Kotlin Standard Library & Coroutines",
            version = "2.2.10 / 1.9.0",
            purpose = "Kotlin runtime & asynchronous reactive coroutine dispatcher",
            license = "Apache 2.0",
            copyright = "Copyright 2010-2025 JetBrains s.r.o. and Kotlin Coroutines contributors",
            projectUrl = "https://kotlinlang.org",
            licenseText = "Apache License Version 2.0, January 2004\nhttp://www.apache.org/licenses/\n\nLicensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except in compliance with the License."
        ),
        DependencyAttribution(
            name = "AndroidX Jetpack Compose & Material 3",
            version = "BOM 2026.02.01",
            purpose = "Declarative UI toolkit, graphics canvas, typography & Material 3 design tokens",
            license = "Apache 2.0",
            copyright = "Copyright The Android Open Source Project",
            projectUrl = "https://developer.android.com/jetpack/compose",
            licenseText = "Apache License Version 2.0, January 2004\nhttp://www.apache.org/licenses/\n\nLicensed under the Apache License, Version 2.0."
        ),
        DependencyAttribution(
            name = "AndroidX Media & MediaSession",
            version = "1.7.0 / Core 1.19.0",
            purpose = "MediaSessionCompat, lock-screen playback controls, and background media notifications",
            license = "Apache 2.0",
            copyright = "Copyright The Android Open Source Project",
            projectUrl = "https://developer.android.com/jetpack/androidx/releases/media",
            licenseText = "Apache License Version 2.0, January 2004\nhttp://www.apache.org/licenses/\n\nLicensed under the Apache License, Version 2.0."
        ),
        DependencyAttribution(
            name = "AndroidX Lifecycle & Activity Compose",
            version = "2.11.0 / 1.13.0",
            purpose = "Lifecycle management, StateFlow ViewModel state, and edge-to-edge Compose hosting",
            license = "Apache 2.0",
            copyright = "Copyright The Android Open Source Project",
            projectUrl = "https://developer.android.com/jetpack/androidx/releases/lifecycle",
            licenseText = "Apache License Version 2.0, January 2004\nhttp://www.apache.org/licenses/\n\nLicensed under the Apache License, Version 2.0."
        ),
        DependencyAttribution(
            name = "OkHttp & Okio",
            version = "4.12.0 / 3.9.1",
            purpose = "HTTP network transport, connection pooling & I/O stream processing",
            license = "Apache 2.0",
            copyright = "Copyright 2019 Square, Inc.",
            projectUrl = "https://square.github.io/okhttp",
            licenseText = "Apache License Version 2.0, January 2004\nhttp://www.apache.org/licenses/\n\nLicensed under the Apache License, Version 2.0."
        ),
        DependencyAttribution(
            name = "Jsoup HTML Parser",
            version = "1.22.2",
            purpose = "HTML structure extraction and document parsing for metadata resolution",
            license = "MIT License",
            copyright = "Copyright © 2009-2025 Jonathan Hedley (jonathan@hedley.net)",
            projectUrl = "https://jsoup.org",
            licenseText = "The MIT License\nCopyright (c) 2009-2025 Jonathan Hedley\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software..."
        ),
        DependencyAttribution(
            name = "Rhino JavaScript Engine",
            version = "1.8.1",
            purpose = "JavaScript runtime environment for YouTube signature cipher deciphering",
            license = "MPL 2.0",
            copyright = "Copyright © 1997-2025 Mozilla Foundation and contributors",
            projectUrl = "https://github.com/mozilla/rhino",
            licenseText = "Mozilla Public License Version 2.0\nhttps://www.mozilla.org/MPL/2.0/\n\nThis Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0."
        ),
        DependencyAttribution(
            name = "Protocol Buffers Java Lite",
            version = "4.35.1",
            purpose = "Efficient structured data serialization format",
            license = "BSD 3-Clause",
            copyright = "Copyright 2008 Google Inc.",
            projectUrl = "https://protobuf.dev",
            licenseText = "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n1. Redistributions of source code must retain the above copyright notice..."
        ),
        DependencyAttribution(
            name = "Desugar JDK Libs",
            version = "2.1.4",
            purpose = "Java 17/NIO modern API desugaring on Android API 26+",
            license = "Apache 2.0",
            copyright = "Copyright The Android Open Source Project",
            projectUrl = "https://github.com/google/desugar_jdk_libs",
            licenseText = "Apache License Version 2.0, January 2004\nhttp://www.apache.org/licenses/\n\nLicensed under the Apache License, Version 2.0."
        )
    )
}
