package cn.remix.module.impl.render;

import cn.remix.module.Category;
import cn.remix.module.Module;
import cn.remix.module.value.impl.BoolValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Переводчик интерфейса.
 *
 * <p>После включения этого модуля названия модулей, настроек и категорий
 * в ClickGUI, ModuleList, TabGUI и уведомлениях отображаются на русском языке.
 * После выключения — возвращаются к английским оригиналам.</p>
 *
 * <p>Важно: модуль влияет только на «отображение», не меняет {@link Module#getName()} и другие
 * реальные поля. Конфиги и чат-команды (например, {@code .toggle Aura}) по-прежнему используют
 * английские оригиналы, поэтому смена языка не ломает существующие настройки и команды.</p>
 */
public final class Translator extends Module {

    private final BoolValue translateModules = new BoolValue("Translate Modules", true);
    private final BoolValue translateValues = new BoolValue("Translate Values", true);
    private final BoolValue translateCategory = new BoolValue("Translate Category", true);
    private final BoolValue splitCamelCase = new BoolValue("Split Camel Case", true);

    public Translator() {
        super("Translator", Category.Render);
        setEnabled(false);
    }

    @Override
    public void onEnable() {
        setSuffix("RU");
    }

    @Override
    public void onDisable() {
        setSuffix("");
    }

    private static Translator instance() {
        if (instance == null || instance.getModuleManager() == null) {
            return null;
        }
        return instance.getModuleManager().getModule(Translator.class);
    }

    public static boolean active() {
        Translator translator = instance();
        return translator != null && translator.isEnabled();
    }

    private static boolean activeValues() {
        Translator translator = instance();
        return translator != null && translator.isEnabled() && translator.translateValues.getValue();
    }

    private static boolean activeModules() {
        Translator translator = instance();
        return translator != null && translator.isEnabled() && translator.translateModules.getValue();
    }

    private static boolean activeCategory() {
        Translator translator = instance();
        return translator != null && translator.isEnabled() && translator.translateCategory.getValue();
    }

    private static boolean camelSplit() {
        Translator translator = instance();
        return translator == null || translator.splitCamelCase.getValue();
    }

    public static String module(String name) {
        if (name == null || name.isEmpty()) return name;
        if (!activeModules()) return name;

        String key = camelSplit() ? splitCamelCase(name) : name;
        String translated = MODULES.get(key);
        if (translated == null) {
            translated = MODULES.get(name);
        }
        return translated == null ? key : translated;
    }

    public static String value(String name) {
        if (name == null || name.isEmpty()) return name;
        if (!activeValues()) return name;

        String translated = VALUES.get(name);
        return translated == null ? name : translated;
    }

    public static String category(String name) {
        if (name == null || name.isEmpty()) return name;
        if (!activeCategory()) return name;

        String translated = CATEGORIES.get(name);
        return translated == null ? name : translated;
    }

    public static String text(String literal) {
        if (literal == null || literal.isEmpty()) return literal;
        if (!active()) return literal;

        String translated = MISC.get(literal);
        return translated == null ? literal : translated;
    }

    public static String splitCamelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.equals(name.toUpperCase())) return name;
        return name.replaceAll("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", " ");
    }

    private static final Map<String, String> MODULES = new LinkedHashMap<>();
    private static final Map<String, String> VALUES = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, String> MISC = new LinkedHashMap<>();

    private static void m(String en, String ru) { MODULES.put(en, ru); }
    private static void v(String en, String ru) { VALUES.put(en, ru); }
    private static void c(String en, String ru) { CATEGORIES.put(en, ru); }
    private static void x(String en, String ru) { MISC.put(en, ru); }

    static {
        // ---------- Категории ----------
        c("Combat", "Бой");
        c("Exploits", "Эксплойты");
        c("Exploit", "Эксплойты");
        c("Move", "Движение");
        c("Movement", "Движение");
        c("Player", "Игрок");
        c("World", "Мир");
        c("Misc", "Разное");
        c("Render", "Визуал");

        // ---------- Модули ----------
        m("Action Recorder", "Запись действий");
        m("Aim Assist", "Помощь в прицеливании");
        m("Air Jump", "Прыжок в воздухе");
        m("Animation", "Анимация");
        m("Anti Blindness", "Анти-слепота");
        m("Anti Bot", "Анти-бот");
        m("Anti Cheat Detect", "Детект анти-чита");
        m("Anti Fireball", "Анти-фаербол");
        m("Anti Hunger", "Анти-голод");
        m("Anti Lava", "Анти-лава");
        m("Anti Nausea", "Анти-тошнота");
        m("Anti Void", "Анти-пустота");
        m("Aura", "Аура");
        m("Auto Armor", "Авто-броня");
        m("Auto Clicker", "Авто-кликер");
        m("Auto Gapple", "Авто-гэппл");
        m("Auto Heal", "Авто-лечение");
        m("Auto Mace", "Авто-булава");
        m("Auto Tool", "Авто-инструмент");
        m("Backtrack", "Возврат");
        m("Better Chat", "Улучшенный чат");
        m("Better Tab", "Улучшенный таб");
        m("Blink", "Блинк");
        m("Block Selection", "Выделение блока");
        m("Brightness", "Яркость");
        m("Button Renderer", "Отрисовка кнопок");
        m("Chest ESP", "Сундуки ESP");
        m("Chest Gui", "GUI сундуков");
        m("Chest Stealer", "Кража сундуков");
        m("Click Gui", "ClickGUI");
        m("Click TP", "Телепорт по клику");
        m("Client Spoof", "Спуф клиента");
        m("Clip HUD", "Clip HUD");
        m("Clipper", "Клиппер");
        m("Clutch", "Клатч");
        m("Compass", "Компас");
        m("Criticals", "Криты");
        m("Crosshair", "Прицел");
        m("Crystal Aura", "Кристалл-аура");
        m("Damage Tint", "Оттенок урона");
        m("Derp", "Дерп");
        m("Disabler", "Дизейблер");
        m("Dynamic Island", "Динамический остров");
        m("EC Disabler", "EC дизейблер");
        m("EC Fly", "EC полёт");
        m("Effect Display", "Отображение эффектов");
        m("ESP", "ESP");
        m("Fast Use", "Быстрое использование");
        m("Fast Web", "Быстрая паутина");
        m("Fly", "Полёт");
        m("Fly Plus", "Полёт+");
        m("Fullbright", "Полная яркость");
        m("Ghost Hand", "Рука-призрак");
        m("Glow", "Свечение");
        m("Glow ESP", "Свечение ESP");
        m("Gui Move", "Движение в GUI");
        m("HUD", "HUD");
        m("Indicator", "Индикатор");
        m("Inventory Manager", "Менеджер инвентаря");
        m("Item Physics", "Физика предметов");
        m("Item Tags", "Теги предметов");
        m("Keep Sprint", "Удержание спринта");
        m("Kill Effects", "Эффекты убийства");
        m("Kill Say", "Сообщение при убийстве");
        m("Legit No Fall", "Легит анти-падение");
        m("Lightning Tracker", "Трекер молний");
        m("Liquid Glass", "Жидкое стекло");
        m("Liquid Glow", "Жидкое свечение");
        m("Mace PVP", "Булава PVP");
        m("MCF", "MCF");
        m("More Particles", "Больше частиц");
        m("Motion Camera", "Динамическая камера");
        m("Mouse Lock", "Блокировка мыши");
        m("Music Player", "Музыкальный плеер");
        m("Name Protect", "Защита имени");
        m("Name Tags", "Никнеймы");
        m("No Fall", "Анти-падение");
        m("No Fall 2", "Анти-падение 2");
        m("No Hurt Cam", "Без тряски камеры");
        m("No Render", "Отключить рендер");
        m("No Slow Down", "Без замедления");
        m("Notification", "Уведомления");
        m("Panic", "Паника");
        m("Phase", "Фейз");
        m("Projectile", "Предупреждение о снарядах");
        m("Protocol", "Протокол");
        m("Regen", "Регенерация");
        m("Reset VL", "Сброс VL");
        m("Safe Walk", "Безопасная ходьба");
        m("Scaffold", "Скаффолд");
        m("Snowball Aura", "Снежковая аура");
        m("Song Info", "Информация о треке");
        m("Spammer", "Спаммер");
        m("Speed", "Скорость");
        m("Sprint", "Спринт");
        m("Step", "Шаг");
        m("Strafe", "Стрейф");
        m("Stuck", "Застревание");
        m("Sword Gapple", "Меч/Гэппл");
        m("Sword Pearl", "Меч/Жемчуг");
        m("Target ESP", "ESP цели");
        m("Target Glow", "Свечение цели");
        m("Targets", "Цели");
        m("Target Strafe", "Стрейф вокруг цели");
        m("TAS", "TAS");
        m("Teams", "Команды");
        m("Teleport", "Телепорт");
        m("Time Changer", "Изменение времени");
        m("Timer", "Таймер");
        m("Title", "Заголовок");
        m("Tp Aura", "ТП-аура");
        m("Tp Aura Plus", "ТП-аура+");
        m("Tpaura Rise", "ТП-аура Rise");
        m("Translator", "Переводчик");
        m("VClip", "VClip");
        m("Velocity", "Анти-отдача");
        m("World Tweaks", "Твики мира");
        m("AutoBlock", "Авто-блок");
        m("TpAura", "ТП-аура");
        m("TpAuraPlus", "ТП-аура+");
        m("TpauraRise", "ТП-аура Rise");
        m("NoFall2", "Анти-падение 2");
        m("ChestGUI", "GUI сундуков");
        m("ClickGUI", "ClickGUI");
        m("RealTime", "Реальное время");

        // ---------- Настройки ----------
        v("1.9 Cooldown", "Кулдаун 1.9");
        v("2D ESP", "2D ESP");
        v("Accent", "Акцент");
        v("Accent Color", "Цвет акцента");
        v("Actionbar", "Экшенбар");
        v("Aim Range", "Дистанция прицела");
        v("All Items", "Все предметы");
        v("Amount", "Количество");
        v("Animals", "Животные");
        v("Animation", "Анимация");
        v("Animation Speed", "Скорость анимации");
        v("Anti Teleport", "Анти-телепорт");
        v("Attack Animals", "Атаковать животных");
        v("Attack Mobs", "Атаковать мобов");
        v("Attack Player", "Атаковать игроков");
        v("Attack Range", "Дистанция атаки");
        v("Attack Delay", "Задержка атаки");
        v("Aura Range", "Дистанция ауры");
        v("Auto Disable", "Авто-отключение");
        v("Auto Jump", "Авто-прыжок");
        v("Auto Totem", "Авто-тотем");
        v("Background Alpha", "Прозрачность фона");
        v("Bar Width", "Ширина полосы");
        v("Block Range", "Дистанция установки");
        v("Blue", "Синий");
        v("Blur", "Размытие");
        v("Border", "Граница");
        v("Chance", "Шанс");
        v("Chest", "Сундук");
        v("Color", "Цвет");
        v("Cooldown", "Кулдаун");
        v("CPS", "Клики в секунду");
        v("Damage", "Урон");
        v("Delay", "Задержка");
        v("Delay Max", "Макс. задержка");
        v("Delay Min", "Мин. задержка");
        v("Distance", "Дистанция");
        v("Duration", "Длительность");
        v("Enabled", "Включено");
        v("Enable Color", "Включить цвет");
        v("Enable Text", "Включить текст");
        v("Ender Pearl", "Эндер-жемчуг");
        v("Fall Distance", "Дистанция падения");
        v("Friend Color", "Цвет друзей");
        v("Green", "Зелёный");
        v("Health", "Здоровье");
        v("Health Bar", "Полоса здоровья");
        v("Height", "Высота");
        v("Hidden FOV", "Скрытый FOV");
        v("Highlight", "Подсветка");
        v("HUD Scale", "Масштаб HUD");
        v("Hurt Time", "Время урона");
        v("Ignore Teammates", "Игнорировать союзников");
        v("Intensity", "Интенсивность");
        v("Invisible", "Невидимые");
        v("Jump Height", "Высота прыжка");
        v("Keep Sprint", "Удержание спринта");
        v("Max CPS", "Макс. CPS");
        v("Max Distance", "Макс. дистанция");
        v("Max Packets", "Макс. пакетов");
        v("Max Players", "Макс. игроков");
        v("Max Ticks", "Макс. тиков");
        v("Message", "Сообщение");
        v("Min CPS", "Мин. CPS");
        v("Min Distance", "Мин. дистанция");
        v("Mode", "Режим");
        v("Module Notify", "Уведомления модулей");
        v("Mobs", "Мобы");
        v("Opacity", "Непрозрачность");
        v("Particle Count", "Количество частиц");
        v("Particles", "Частицы");
        v("Pause On Hurt", "Пауза при уроне");
        v("Pearl Slot", "Слот жемчуга");
        v("Place Range", "Дистанция установки");
        v("Player", "Игрок");
        v("Players", "Игроки");
        v("Potion Effects", "Эффекты зелий");
        v("Prefix", "Префикс");
        v("Protect Friends", "Защищать друзей");
        v("Protect Self", "Защищать себя");
        v("Radius", "Радиус");
        v("Range", "Дистанция");
        v("Red", "Красный");
        v("Render", "Отрисовка");
        v("Rotation Speed", "Скорость поворота");
        v("Scale", "Масштаб");
        v("Show CPS", "Показывать CPS");
        v("Show FPS", "Показывать FPS");
        v("Show Health", "Показывать здоровье");
        v("Show Ping", "Показывать пинг");
        v("Show Player", "Показывать игрока");
        v("Show Time", "Показывать время");
        v("Show User", "Показывать ник");
        v("Silent", "Тихий");
        v("Size", "Размер");
        v("Sound", "Звук");
        v("Speed", "Скорость");
        v("Sprint", "Спринт");
        v("Style", "Стиль");
        v("Target", "Цель");
        v("Target Range", "Дистанция цели");
        v("Text", "Текст");
        v("Time", "Время");
        v("Timer", "Таймер");
        v("Times", "Раз");
        v("Vertical", "Вертикально");
        v("Volume", "Громкость");
        v("Wall Range", "Дистанция через стены");
        v("Width", "Ширина");
        v("X", "X");
        v("X Offset", "Смещение X");
        v("Y", "Y");
        v("Y Offset", "Смещение Y");
        v("Show Seconds", "Показывать секунды");
        v("Shadow", "Тень");
        v("Translate Modules", "Перевод модулей");
        v("Translate Values", "Перевод настроек");
        v("Translate Category", "Перевод категорий");
        v("Split Camel Case", "Разделять CamelCase");

        // ---------- ClickGUI ----------
        x("Bind", "Бинд");
        x("Enabled", "Включено");
        x("Disabled", "Отключено");
        x("ON", "ВКЛ");
        x("OFF", "ВЫКЛ");
    }
}