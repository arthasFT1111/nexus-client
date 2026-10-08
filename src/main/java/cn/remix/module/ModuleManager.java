package cn.remix.module;

import cn.remix.Client;
import cn.remix.event.base.annotation.EventTarget;
import cn.remix.event.impl.KeyInputEvent;
import cn.remix.module.impl.combat.*;
import cn.remix.module.impl.exploits.Disabler;
import cn.remix.module.impl.exploits.ECDisabler;
import cn.remix.module.impl.exploits.Phase;
import cn.remix.module.impl.exploits.Regen;
import cn.remix.module.impl.exploits.ResetVL;
import cn.remix.module.impl.misc.AntiCheatDetect;
import cn.remix.module.impl.misc.AntiFireball;
import cn.remix.module.impl.misc.BetterChat;
import cn.remix.module.impl.misc.BetterTab;
import cn.remix.module.impl.misc.ClientSpoof;
import cn.remix.module.impl.misc.KillSay;
import cn.remix.module.impl.misc.NameProtect;
import cn.remix.module.impl.misc.Panic;
import cn.remix.module.impl.misc.Spammer;
import cn.remix.module.impl.move.*;
import cn.remix.module.impl.player.*;
import cn.remix.module.impl.render.*;
import cn.remix.module.impl.world.BedBreaker;
import cn.remix.module.impl.world.Clutch;
import cn.remix.module.impl.world.Protocol;
import cn.remix.module.impl.world.Scaffold;
import cn.remix.module.impl.world.Tas;
import cn.remix.module.impl.world.Timer;
import cn.remix.module.impl.world.WorldTweaks;
import cn.remix.module.value.Value;
import cn.remix.util.IMinecraft;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.*;

@Getter
public class ModuleManager implements IMinecraft {
    private final Map<String, Module> moduleMap = new LinkedHashMap<>();
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        // ─── Combat ───
        DESCRIPTIONS.put("Aura", "Автоматически атакует врагов в радиусе");
        DESCRIPTIONS.put("AimAssist", "Помогает прицеливаться по цели");
        DESCRIPTIONS.put("AutoClicker", "Автоматически кликает с заданной скоростью");
        DESCRIPTIONS.put("AutoMace", "Автоматически использует булаву");
        DESCRIPTIONS.put("MacePVP", "Помощь в PvP с булавой");
        DESCRIPTIONS.put("AutoHeal", "Автоматически лечится зельями и едой");
        DESCRIPTIONS.put("AttackCrystal", "Автоматически атакует кристаллы Края");
        DESCRIPTIONS.put("SnowballAura", "Автоматически кидает снежки в врагов");
        DESCRIPTIONS.put("Criticals", "Наносит только критические удары");
        DESCRIPTIONS.put("Velocity", "Убирает или уменьшает отдачу");
        DESCRIPTIONS.put("AutoGapple", "Автоматически ест золотые яблоки");
        DESCRIPTIONS.put("SwordGapple", "Автоматически переключается на меч/гэппл");
        DESCRIPTIONS.put("SwordPearl", "Автоматически переключается на меч/жемчуг");
        DESCRIPTIONS.put("AntiBot", "Игнорирует ботов при атаке");
        DESCRIPTIONS.put("PearlCatch", "Автоматически ловит жемчуг");
        DESCRIPTIONS.put("Targets", "Настройка целей для ауры");
        DESCRIPTIONS.put("Teams", "Не атакует союзников");
        DESCRIPTIONS.put("Derp", "Вращает голову для обхода античитов");

        // ─── Exploits ───
        DESCRIPTIONS.put("Disabler", "Отключает проверки античита");
        DESCRIPTIONS.put("ECDisabler", "Дизейблер для Ender Chest");
        DESCRIPTIONS.put("Phase", "Проходит сквозь блоки");
        DESCRIPTIONS.put("ResetVL", "Сбрасывает нарушения (VL)");
        DESCRIPTIONS.put("Regen", "Ускоряет регенерацию здоровья");

        // ─── Move ───
        DESCRIPTIONS.put("Speed", "Ускоряет передвижение");
        DESCRIPTIONS.put("Fly", "Полёт в режиме выживания");
        DESCRIPTIONS.put("FlyPlus", "Улучшенный полёт с настройками");
        DESCRIPTIONS.put("ECFly", "Полёт через Ender Chest");
        DESCRIPTIONS.put("Strafe", "Автоматический стрейф в воздухе");
        DESCRIPTIONS.put("Step", "Автоматически поднимается на блоки");
        DESCRIPTIONS.put("AirJump", "Прыжки в воздухе");
        DESCRIPTIONS.put("NoSlowDown", "Убирает замедление от еды, блоков и т.д.");
        DESCRIPTIONS.put("NoFall", "Убирает урон от падения");
        DESCRIPTIONS.put("LegitNoFall", "Легитный анти-фол (менее заметный)");
        DESCRIPTIONS.put("SafeWalk", "Не даёт упасть с края блока");
        DESCRIPTIONS.put("Sprint", "Автоматический спринт");
        DESCRIPTIONS.put("KeepSprint", "Сохраняет спринт при ударе");
        DESCRIPTIONS.put("AntiVoid", "Спасает от падения в пустоту");
        DESCRIPTIONS.put("FastWeb", "Быстрое передвижение в паутине");
        DESCRIPTIONS.put("Blink", "Задерживает пакеты для телепортации");
        DESCRIPTIONS.put("Timer", "Ускоряет игровое время");
        DESCRIPTIONS.put("Tas", "Инструменты для TAS-спидранов");
        DESCRIPTIONS.put("Clipper", "Клип через блоки");
        DESCRIPTIONS.put("VClip", "Вертикальный клип через блоки");
        DESCRIPTIONS.put("ClickTP", "Телепорт по клику мыши");
        DESCRIPTIONS.put("Teleport", "Телепортация в заданную точку");
        DESCRIPTIONS.put("GuiMove", "Движение при открытом GUI");
        DESCRIPTIONS.put("MouseLock", "Блокировка мыши в GUI");
        DESCRIPTIONS.put("TargetStrafe", "Стрейф вокруг цели");

        // ─── Player ───
        DESCRIPTIONS.put("AutoArmor", "Автоматически надевает лучшую броню");
        DESCRIPTIONS.put("AutoTool", "Автоматически выбирает нужный инструмент");
        DESCRIPTIONS.put("FastUse", "Быстрое использование предметов");
        DESCRIPTIONS.put("ChestStealer", "Автоматически крадёт предметы из сундуков");
        DESCRIPTIONS.put("InventoryManager", "Автоматически сортирует инвентарь");
        DESCRIPTIONS.put("AntiHunger", "Убирает голод");
        DESCRIPTIONS.put("AntiLava", "Защита от лавы");
        DESCRIPTIONS.put("GhostHand", "Взаимодействие с блоками через стены");
        DESCRIPTIONS.put("Stuck", "Помощь при застревании в блоках");

        // ─── World ───
        DESCRIPTIONS.put("Scaffold", "Автоматически ставит блоки под ноги");
        DESCRIPTIONS.put("BedBreaker", "Автоматически ломает кровати");
        DESCRIPTIONS.put("Clutch", "Спасает от падения (ставит блоки/воду)");
        DESCRIPTIONS.put("WorldTweaks", "Твики мира (время, погода)");
        DESCRIPTIONS.put("TimeChanger", "Изменяет время суток");
        DESCRIPTIONS.put("Protocol", "Работа с протоколом сервера");

        // ─── Misc ───
        DESCRIPTIONS.put("BetterChat", "Улучшает чат (история, копирование)");
        DESCRIPTIONS.put("BetterTab", "Улучшает таб-лист игроков");
        DESCRIPTIONS.put("NameProtect", "Скрывает твой ник");
        DESCRIPTIONS.put("KillSay", "Отправляет сообщение при убийстве");
        DESCRIPTIONS.put("Spammer", "Спамит сообщениями в чат");
        DESCRIPTIONS.put("Panic", "Быстро выключает все модули");
        DESCRIPTIONS.put("AntiFireball", "Защита от фаерболов");
        DESCRIPTIONS.put("AntiCheatDetect", "Определяет античит на сервере");
        DESCRIPTIONS.put("ClientSpoof", "Подменяет данные клиента");
        DESCRIPTIONS.put("ActionRecorder", "Записывает твои действия");

        // ─── Render ───
        DESCRIPTIONS.put("HUD", "Основной HUD клиента");
        DESCRIPTIONS.put("ClickGui", "Меню чита (Right Shift)");
        DESCRIPTIONS.put("Notification", "Уведомления о событиях");
        DESCRIPTIONS.put("ESP", "Подсвечивает сущности через стены");
        DESCRIPTIONS.put("GlowESP", "Свечение сущностей через стены");
        DESCRIPTIONS.put("ChestESP", "Подсвечивает сундуки");
        DESCRIPTIONS.put("TargetESP", "Подсвечивает цель ауры");
        DESCRIPTIONS.put("TargetGlow", "Свечение цели ауры");
        DESCRIPTIONS.put("TargetHUD", "Информация о цели на экране");
        DESCRIPTIONS.put("NameTags", "Никнеймы над игроками");
        DESCRIPTIONS.put("ItemTags", "Теги предметов");
        DESCRIPTIONS.put("ModuleList", "Список включённых модулей");
        DESCRIPTIONS.put("Indicator", "Индикатор направления");
        DESCRIPTIONS.put("Crosshair", "Кастомный прицел");
        DESCRIPTIONS.put("Compass", "Компас на экране");
        DESCRIPTIONS.put("EffectDisplay", "Активные эффекты зелий");
        DESCRIPTIONS.put("ItemPhysics", "Физика предметов на земле");
        DESCRIPTIONS.put("MotionCamera", "Динамическая камера");
        DESCRIPTIONS.put("Fullbright", "Полная яркость (видно в темноте)");
        DESCRIPTIONS.put("Brightness", "Настройка яркости");
        DESCRIPTIONS.put("NoHurtCam", "Убирает тряску камеры при уроне");
        DESCRIPTIONS.put("NoRender", "Отключает ненужный рендер");
        DESCRIPTIONS.put("AntiBlindness", "Убирает эффект слепоты");
        DESCRIPTIONS.put("AntiNausea", "Убирает эффект тошноты");
        DESCRIPTIONS.put("DamageTint", "Оттенок при получении урона");
        DESCRIPTIONS.put("BlockSelection", "Подсветка выделенного блока");
        DESCRIPTIONS.put("KillEffects", "Эффекты при убийстве");
        DESCRIPTIONS.put("Animation", "Кастомные анимации руки");
        DESCRIPTIONS.put("Translator", "Перевод интерфейса на русский");
        DESCRIPTIONS.put("Glow", "Свечение сущностей");
        DESCRIPTIONS.put("LightningTracker", "Отслеживает молнии");
        DESCRIPTIONS.put("ChestGUI", "Просмотр содержимого сундуков");
    }

    public ModuleManager() {
        instance.getEventManager().register(this);

        addModules(
                new HUD(),
                new Notification(),
                new ClickGui(),
                new Scaffold(),
                new BedBreaker(),
                new Protocol(),
                new Clutch(),
                new WorldTweaks(),
                new AntiBot(),
                new Aura(),
                new Targets(),
                new Teams(),
                new Derp(),
                new Disabler(),
                new GuiMove(),
                new Teleport(),
                new Clipper(),
                new VClip(),
                new FastWeb(),
                new SafeWalk(),
                new TargetStrafe(),
                new DamageTint(),
                new BlockSelection(),
                new Criticals(),
                new NoSlowDown(),
                new NoFall(),
                new AntiVoid(),
                new Blink(),
                new ECFly(),
                new LegitNoFall(),
                new ModuleList(),
                new Speed(),
                new Fly(),
                new Velocity(),
                new AutoTool(),
                new FastUse(),
                new SwordGapple(),
                new ChestStealer(),
                new TargetHUD(),
                new InventoryManager(),
                new MotionCamera(),
                new AutoArmor(),
                new AntiHunger(),
                new Crosshair(),
                new LightningTracker(),
                new Regen(),
                new Brightness(),
                new NoHurtCam(),
                new ItemPhysics(),
                new KeepSprint(),
                new Animation(),
                new ESP(),
                new GlowESP(),
                new Indicator(),
                new ChestESP(),
                new AutoGapple(),
                new GhostHand(),
                new Step(),
                new KillEffects(),
                new Stuck(),
                new Strafe(),
                new ClickTP(),
                new AntiLava(),
                new Sprint(),
                new Fullbright(),
                new ResetVL(),
                new Phase(),
                new ECDisabler(),
                new ChestGUI(),
                new MouseLock(),
                new BetterChat(),
                new NameProtect(),
                new BetterTab(),
                new AirJump(),
                new AntiCheatDetect(),
                new AntiFireball(),
                new ClientSpoof(),
                new Panic(),
                new KillSay(),
                new Spammer(),
                new FlyPlus(),
                new Timer(),
                new Tas(),
                new AutoMace(),
                new SwordPearl(),
                new PearlCatch(),
                new ActionRecorder(),
                new AimAssist(),
                new AutoHeal(),
                new AutoClicker(),
                new AttackCrystal(),
                new SnowballAura(),
                new MacePVP(),
                new Compass(),
                new EffectDisplay(),
                new ItemTags(),
                new NameTags(),
                new TimeChanger(),
                new Glow(),
                new TargetESP(),
                new TargetGlow(),
                new AntiBlindness(),
                new TargetOutline(),
                new AntiNausea(),
                new NoRender(),
                new Translator()
        );

        sortModules();
    }

    public void addModules(Module... modulesArray) {
        for (Module module : modulesArray) {
            reflectModuleValues(module);
            String desc = DESCRIPTIONS.get(module.getName());
            if (desc != null) module.setDescription(desc);
            moduleMap.put(module.getClass().getSimpleName(), module);
        }
    }

    private void reflectModuleValues(Module module) {
        try {
            Class<?> clazz = module.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (Value.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object valueObject = field.get(module);
                        if (valueObject != null) {
                            module.getValues().add((Value) valueObject);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            Client.logger.debug(e.getMessage());
        }
    }

    private void sortModules() {
        List<Module> moduleList = new ArrayList<>(moduleMap.values());
        moduleList.sort(Comparator.comparing(Module::getName));
        moduleMap.clear();
        for (Module module : moduleList) {
            moduleMap.put(module.getClass().getSimpleName(), module);
        }
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return clazz.cast(moduleMap.get(clazz.getSimpleName()));
    }

    @EventTarget
    private void onKeyInput(KeyInputEvent event) {
        if (event.getKey() == 0 || mc.currentScreen != null) return;

        for (Module module : moduleMap.values()) {
            if (module.getKey() == event.getKey()) {
                module.toggle();
            }
        }
    }
}