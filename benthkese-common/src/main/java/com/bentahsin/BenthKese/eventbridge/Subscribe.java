package com.bentahsin.BenthKese.eventbridge;

import org.bukkit.event.EventPriority;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BenthEventBridge dinleyicilerini işaretlemek için kullanılır.
 * Bukkit'in standart @EventHandler'ının yerini alır.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * Event önceliği.
     * Default: NORMAL
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Eğer true ise, başka bir eklenti event'i iptal etse bile (setCancelled(true))
     * bu metod çalışmaya devam etmez (Bukkit standardı).
     * Ancak "true" yaparsanız iptal edilmiş eventleri görmezden gelirsiniz.
     */
    boolean ignoreCancelled() default false;

    /**
     * Sadece BenthMessageEvent için geçerlidir.
     * Eğer belirtilirse, sadece bu kanal ismine sahip mesajlar tetiklenir.
     * Boş bırakılırsa tüm kanalları dinler (manuel kontrol gerekir).
     */
    String channel() default "";
}
