package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.model.resources.UserRestriction

class UserRestrictionTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    UserRestriction::class.java to mapOf("id" to 's', "capability" to 's', "reason" to 's', "startsAt" to 's'),
))
