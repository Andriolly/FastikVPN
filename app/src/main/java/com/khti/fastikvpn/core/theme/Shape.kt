package com.khti.fastikvpn.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.khti.fastikvpn.core.constants.Dimens

/**
 * Скругления компонентов. Значения берутся из Dimens (радиусы с дашборда):
 *  small  - карточка IP и нижнее меню;
 *  medium - кнопки.
 */
val AppShapes = Shapes(
    small = RoundedCornerShape(Dimens.IpCardRadius),
    medium = RoundedCornerShape(Dimens.EnterButtonRadius),
    large = RoundedCornerShape(Dimens.BottomBarRadius)
)