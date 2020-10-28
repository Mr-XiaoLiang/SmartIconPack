package com.lollipop.iconcore.util

/**
 * @author lollipop
 * @date 10/27/20 02:28
 * 制作者信息管理类
 */
class MakerInfoManager(private val provider: MakerInfoProvider?) {

    /**
     * 作者logo
     */
    val icon: Int
        get() {
            return provider?.icon?:0
        }

    /**
     * 作者名称
     */
    val name: Int
        get() {
            return provider?.name?:0
        }

    /**
     * 作者签名
     */
    val signature: Int
        get() {
            return provider?.signature?:0
        }

    /**
     * 作者格言的字符串数组
     */
    val mottoArray: Int
        get() {
            return provider?.mottoArray?:0
        }

    /**
     * 作者个人页面的背景图
     */
    val background: Int
        get() {
            return provider?.background?:0
        }

    /**
     * 作者的电子邮箱
     */
    val email: Int
        get() {
            return provider?.email?:0
        }

    /**
     * 当前应用使用的图标
     */
    val appIcon: Int
        get() {
            return provider?.appIcon?:0
        }

    interface MakerInfoProvider {

        /**
         * 制作者的头像
         * 这是一个drawable图片的id
         */
        val icon: Int

        /**
         * 制作者的名称
         * 这是一个string字符串的id
         * 这里使用id的原因是为了国际化
         */
        val name: Int

        /**
         * 制作者的签名
         * 这是一个string字符串的id
         * 这里使用id的原因是为了国际化
         */
        val signature: Int

        /**
         * 制作者的格言
         * 这是一个string-array字符串数组的id
         * 这里使用id的原因是为了国际化
         *
         * 注意，如果使用的是字符串的id，那么可能会引发崩溃
         */
        val mottoArray: Int

        /**
         * 制作者的背景图信息
         * 这是一个drawable图片的id
         */
        val background: Int

        /**
         * 制作者的电子邮箱
         * 这是一个string字符串的id
         * 这里使用id的原因是为了国际化
         */
        val email: Int

        /**
         * 当前应用的图标
         * 可以是mipmap的图标资源id
         * 建议填写原始的未剪裁的资源ID
         */
        val appIcon: Int
    }

}