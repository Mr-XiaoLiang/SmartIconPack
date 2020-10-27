package com.lollipop.iconcore.util

/**
 * @author lollipop
 * @date 10/27/20 02:28
 */
class MakerInfoManager(private val provider: MakerInfoProvider?) {

    val icon: Int
        get() {
            return provider?.icon?:0
        }

    val name: Int
        get() {
            return provider?.name?:0
        }

    val signature: Int
        get() {
            return provider?.signature?:0
        }

    val mottoArray: Int
        get() {
            return provider?.mottoArray?:0
        }

    val background: Int
        get() {
            return provider?.background?:0
        }

    val email: Int
        get() {
            return provider?.email?:0
        }



    interface MakerInfoProvider {

        val icon: Int

        val name: Int

        val signature: Int

        val mottoArray: Int

        val background: Int

        val email: Int
    }

}