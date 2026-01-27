package com.bianca.moneymind.domain.usecase.ai

import com.bianca.moneymind.domain.model.TransactionType

/**
 * Default categorization rules for auto-categorization
 */
object DefaultRules {

    /**
     * Keyword rule for matching description text
     */
    data class KeywordRule(
        val keywords: List<String>,
        val categoryId: String,
        val type: TransactionType = TransactionType.EXPENSE
    )

    /**
     * Merchant rule for exact merchant matching
     */
    data class MerchantRule(
        val merchantName: String,
        val categoryId: String,
        val type: TransactionType = TransactionType.EXPENSE
    )

    /**
     * Keyword-based categorization rules
     * Priority: More specific keywords first
     */
    val keywordRules = listOf(
        // 餐飲 - 早餐
        KeywordRule(listOf("早餐", "早點", "早食"), "expense_food"),
        // 餐飲 - 午餐
        KeywordRule(listOf("午餐", "午飯", "便當", "中餐"), "expense_food"),
        // 餐飲 - 晚餐
        KeywordRule(listOf("晚餐", "晚飯", "晚食", "消夜", "宵夜"), "expense_food"),
        // 餐飲 - 飲料
        KeywordRule(listOf("咖啡", "拿鐵", "美式", "茶", "奶茶", "飲料", "手搖"), "expense_food"),
        // 餐飲 - 其他
        KeywordRule(listOf("零食", "點心", "小吃", "餐廳", "吃飯"), "expense_food"),

        // 交通 - 大眾運輸
        KeywordRule(listOf("捷運", "公車", "火車", "高鐵", "悠遊卡", "通勤"), "expense_transport"),
        // 交通 - 計程車
        KeywordRule(listOf("uber", "計程車", "小黃", "taxi", "叫車"), "expense_transport"),
        // 交通 - 加油
        KeywordRule(listOf("加油", "油錢", "95", "98", "92"), "expense_transport"),
        // 交通 - 停車
        KeywordRule(listOf("停車", "停車費"), "expense_transport"),

        // 購物
        KeywordRule(listOf("衣服", "鞋子", "包包", "購物", "網購", "蝦皮", "momo"), "expense_shopping"),

        // 娛樂
        KeywordRule(listOf("電影", "遊戲", "KTV", "唱歌", "演唱會", "展覽"), "expense_entertainment"),
        KeywordRule(listOf("netflix", "spotify", "訂閱", "會員"), "expense_entertainment"),

        // 生活
        KeywordRule(listOf("水電", "電費", "水費", "瓦斯", "房租", "管理費"), "expense_living"),
        KeywordRule(listOf("電話費", "網路費", "手機費"), "expense_living"),

        // 醫療
        KeywordRule(listOf("看病", "醫院", "診所", "藥", "掛號"), "expense_medical"),

        // 教育
        KeywordRule(listOf("學費", "補習", "課程", "書", "教材"), "expense_education"),

        // 收入
        KeywordRule(listOf("薪水", "薪資", "月薪", "工資", "發薪"), "income_salary", TransactionType.INCOME),
        KeywordRule(listOf("獎金", "分紅", "年終"), "income_bonus", TransactionType.INCOME),
        KeywordRule(listOf("投資", "股息", "利息", "股票"), "income_investment", TransactionType.INCOME),
        KeywordRule(listOf("外快", "兼職", "接案"), "income_other", TransactionType.INCOME)
    )

    /**
     * Merchant-based categorization rules
     */
    val merchantRules = listOf(
        // 便利商店
        MerchantRule("全家", "expense_food"),
        MerchantRule("7-11", "expense_food"),
        MerchantRule("萊爾富", "expense_food"),
        MerchantRule("OK", "expense_food"),

        // 咖啡店
        MerchantRule("星巴克", "expense_food"),
        MerchantRule("路易莎", "expense_food"),
        MerchantRule("cama", "expense_food"),
        MerchantRule("伯朗咖啡", "expense_food"),

        // 速食
        MerchantRule("麥當勞", "expense_food"),
        MerchantRule("肯德基", "expense_food"),
        MerchantRule("摩斯漢堡", "expense_food"),
        MerchantRule("漢堡王", "expense_food"),
        MerchantRule("必勝客", "expense_food"),
        MerchantRule("達美樂", "expense_food"),

        // 超市
        MerchantRule("全聯", "expense_food"),
        MerchantRule("家樂福", "expense_food"),
        MerchantRule("頂好", "expense_food"),
        MerchantRule("大潤發", "expense_food"),

        // 交通
        MerchantRule("台灣大車隊", "expense_transport"),
        MerchantRule("中油", "expense_transport"),
        MerchantRule("台亞", "expense_transport"),
        MerchantRule("台塑", "expense_transport"),

        // 娛樂
        MerchantRule("威秀", "expense_entertainment"),
        MerchantRule("國賓", "expense_entertainment"),
        MerchantRule("秀泰", "expense_entertainment"),

        // 購物
        MerchantRule("uniqlo", "expense_shopping"),
        MerchantRule("zara", "expense_shopping"),
        MerchantRule("h&m", "expense_shopping"),
        MerchantRule("蝦皮", "expense_shopping"),
        MerchantRule("momo", "expense_shopping"),
        MerchantRule("pchome", "expense_shopping"),

        // 生活繳費
        MerchantRule("台電", "expense_living"),
        MerchantRule("中華電信", "expense_living"),
        MerchantRule("台灣大哥大", "expense_living"),
        MerchantRule("遠傳", "expense_living")
    )
}
