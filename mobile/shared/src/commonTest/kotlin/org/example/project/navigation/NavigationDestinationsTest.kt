package org.example.project.navigation

import org.example.project.core.navigation.Screen
import org.example.project.core.navigation.AppRoutes
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationDestinationsTest {
    @Test
    fun routesStayStableAndInExpectedOrder() {
        val routes = Screen.entries.map { it.route }

        assertEquals(listOf("camera", "docs", "source"), routes)
    }

    @Test
    fun routesAreUnique() {
        val routes = Screen.entries.map { it.route }

        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun historyDetailRouteContainsIdAndSelectsHistoryTab() {
        assertEquals("docs/42", AppRoutes.historyDetail(42))
        assertEquals(Screen.Docs.route, AppRoutes.bottomBarRoute(AppRoutes.HISTORY_DETAIL))
        assertEquals(Screen.CameraView.route, AppRoutes.bottomBarRoute(Screen.CameraView.route))
        assertEquals(null, AppRoutes.bottomBarRoute(null))
    }

    @Test
    fun backButtonAppearsOnlyForNestedDestinationsWithPreviousEntry() {
        assertEquals(true, AppRoutes.showsBackButton(AppRoutes.HISTORY_DETAIL, true))
        assertEquals(false, AppRoutes.showsBackButton(AppRoutes.HISTORY_DETAIL, false))
        assertEquals(false, AppRoutes.showsBackButton(Screen.Docs.route, true))
        assertEquals(false, AppRoutes.showsBackButton(Screen.CameraView.route, true))
        assertEquals(false, AppRoutes.showsBackButton(AppRoutes.AUTH_SIGN_IN, true))
    }
}
