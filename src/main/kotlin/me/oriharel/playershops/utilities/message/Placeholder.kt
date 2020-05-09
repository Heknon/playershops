package me.oriharel.playershops.utilities.message

import me.oriharel.playershops.utilities.Utils.format


class Placeholder {
    val placeholder: String
    val replacement: String?

    constructor(placeholder: String, replacement: String?) {
        this.placeholder = placeholder
        this.replacement = replacement
    }

    constructor(placeholder: String, replacement: Int?) {
        this.placeholder = placeholder
        this.replacement = replacement?.format() ?: "n/a"
    }

    override fun toString(): String {
        return "Placeholder{" +
                "placeholder='" + placeholder + '\'' +
                ", replacement='" + replacement + '\'' +
                '}'
    }
}