package org.example.project.feature.auth.data.mapper

import org.example.project.feature.auth.data.network.models.UserDto
import org.example.project.feature.auth.domain.model.User

internal fun mapTo(userDto: UserDto): User = User(
    id = userDto.id,
    email = userDto.email,
    firstName = userDto.firstName,
    lastName = userDto.lastName,
)
