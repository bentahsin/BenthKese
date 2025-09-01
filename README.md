# BenthKese
[![Sürüm](https://img.shields.io/badge/Version-1.0.0-blue.svg)]()

[![Uyumlu Sürümler](https://img.shields.io/badge/Spigot/Paper-1.13.x%20--%201.20.x-orange.svg)](https://www.spigotmc.org/)
[![Gereklilik](https://img.shields.io/badge/Dependency-Vault-blue)](https://www.spigotmc.org/resources/vault.34315/)
[![İsteğe Bağlı](https://img.shields.io/badge/Optional-PlaceHolderAPI-yellow)](https://www.spigotmc.org/resources/placeholderapi.6245/)
[![Lisans](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Gelişmiş Towny Kese Sistemi**

BenthKese, oyuncuların envanterlerindeki değerli eşyaları (varsayılan olarak altın) sanal bir keseye yatırıp çekmelerini sağlayan modern bir Spigot eklentisidir. Sadece bir eşya-para dönüştürücüsü olmanın ötesinde, tamamen yapılandırılabilir sistemleriyle sunucu ekonominize derinlik ve kontrol katmanları ekler.

Kullanıcı dostu menüleri, güçlü yönetici araçları, şeffaf işlem geçmişi ve geniş PlaceHolderAPI desteği ile BenthKese, her türlü sunucu ekonomisi için mükemmel bir tamamlayıcıdır.

## ✨ Öne Çıkan Özellikler

### 💎 Oyuncu Odaklı Özellikler
*   **Modern Grafik Arayüz (GUI):** `/kese` komutu ile açılan, tüm işlemlerin kolayca yapılabildiği interaktif ve şık bir ana menü.
*   **Esnek Para Yatırma:** Belirli bir miktar (`/kese koy 64`), eldeki tüm eşyalar (`/kese koy el`) veya envanterdeki tüm eşyalar (`/kese koy envanter`) tek komutla keseye yatırılabilir.
*   **Akıllı Para Çekme:** Para çekerken envanteriniz doluysa, eklenti alabileceğiniz maksimum miktarı verir ve sizi bilgilendirir.
*   **İşlem Geçmişi:** Oyuncular, GUI üzerinden son 50 finansal işlemini (para gönderme, alma, limit yükseltme vb.) şeffaf bir şekilde takip edebilir.
*   **Güvenli İşlemler:** Yüksek maliyetli işlemlerden (limit yükseltme, hesabı bozma) önce bir onay menüsü sunarak yanlışlıkla yapılan tıklamaları engeller.

### 🏦 Gelişmiş Sistemler
*   **Seviye Bazlı Limit Sistemi:**
    *   `limits.yml` dosyasından tamamen ayarlanabilir limit seviyeleri oluşturun.
    *   Her seviyenin kendi adı, maliyeti, günlük gönderme ve alma limiti olabilir.
    *   Oyuncular, belirlediğiniz ücreti ödeyerek limit seviyelerini oyun içinden yükseltebilirler.
*   **Vadeli Faiz Sistemi:**
    *   Oyuncular paralarını belirli bir süre (`1d`, `7d` vb.) kilitleyerek, `config.yml` dosyasında tanımlanan oranlara göre faiz geliri elde edebilirler.
    *   Tüm vadeli hesaplar, özel bir GUI üzerinden kolayca yönetilebilir.
*   **Dinamik Butonlar:**
    *   GUI'deki butonlar, oyuncunun durumuna göre dinamik olarak değişir. Örneğin, bir oyuncunun limit yükseltmek için yeterli parası yoksa, buton kırmızıya döner ve nedenini açıklar.

### ⚙️ Yönetici ve Sunucu Özellikleri
*   **Güçlü Yönetim Komutları (`/keseadmin`):**
    *   **Reload:** Sunucuyu yeniden başlatmadan tüm konfigürasyon dosyalarını anında yenileyin.
    *   **Bakiye Yönetimi:** Oyuncuların Vault bakiyelerine para ekleyin, çıkarın veya doğrudan ayarlayın.
    *   **Limit Yönetimi:** Bir oyuncunun limit seviyesini anında değiştirin.
*   **Esnek Veri Depolama:**
    *   Sunucu ihtiyaçlarına göre **YAML**, **SQLite** (varsayılan) veya **MySQL** depolama türlerinden birini seçin. MySQL, yüksek performanslı `HikariCP` bağlantı havuzu ile desteklenir.
*   **Tamamen Özelleştirilebilir:** `messages.yml` dosyası sayesinde eklentideki her bir metni, renk kodları ve değişkenlerle birlikte sunucunuzun konseptine göre düzenleyin.

### 🌐 Entegrasyonlar
*   **Vault (Zorunlu):** Sunucunuzdaki herhangi bir ekonomi eklentisiyle sorunsuz çalışır.
*   **PlaceHolderAPI (İsteğe Bağlı):** Skor tabloları, sohbet formatları ve diğer eklentilerle entegre etmek için kapsamlı placeholder desteği.
*   **AnvilGUI:** Oyuncudan miktar veya isim gibi girdileri almak için modern ve kullanıcı dostu arayüzler kullanır.

## 📦 Kurulum

1.  Eklentinin son sürümünü (`BenthKese-X.X.X.jar`) indirin.
2.  Sunucunuzda **[Vault](https://www.spigotmc.org/resources/vault.34315/)** eklentisinin kurulu olduğundan emin olun.
3.  (İsteğe Bağlı) **[PlaceHolderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** eklentisini kurun.
4.  İndirdiğiniz `.jar` dosyasını sunucunuzun `plugins/` klasörüne atın.
5.  Sunucuyu başlatın. Eklenti, `plugins/BenthKese/` klasörü içinde `config.yml`, `messages.yml` ve `limits.yml` dosyalarını oluşturacaktır.
6.  Dosyaları kendi sunucunuza göre düzenleyin ve ayarları anında yenilemek için `/bka reload` komutunu kullanın.

## 🛠️ Komutlar ve Yetkiler

### Oyuncu Komutları (`/kese`)
| Komut | Açıklama | Yetki |
| --- | --- | --- |
| `/kese` | Ana GUI menüsünü açar. | `benthkese.command.gui` |
| `/kese help` | Yardım menüsünü gösterir. | `benthkese.command.help` |
| `/kese koy [miktar\|el\|envanter]` | Keseye para/altın yatırır. | `benthkese.command.koy` |
| `/kese al [miktar]` | Keseden para/altın çeker. | `benthkese.command.al` |
| `/kese gonder <oyuncu> <miktar>` | Başka bir oyuncuya para gönderir. | `benthkese.command.gonder` |
| `/kese limit` | Tüm limit seviyeleri hakkında bilgi verir. | `benthkese.command.limit.info` |
| `/kese limit gor` | Kişisel limit durumunuzu gösterir. | `benthkese.command.limit.gor` |
| `/kese limit yükselt` | Limit seviyenizi yükseltir. | `benthkese.command.limit.yukselt`|
| `/kese faiz` | Vadeli Faiz Sistemi ana menüsünü açar. | `benthkese.command.faiz` |
| `/kese faiz koy <miktar> <süre>` | Yeni bir vadeli hesap oluşturur. | `benthkese.command.faiz.koy` |

### Yönetici Komutları (`/keseadmin` veya `/bka`)
| Komut | Açıklama | Yetki |
| --- | --- | --- |
| `/bka reload` | Eklenti konfigürasyonlarını yeniden yükler. | `benthkese.admin.reload` |
| `/bka limit set <oyuncu> <seviye>` | Bir oyuncunun limit seviyesini ayarlar. | `benthkese.admin.limit` |
| `/bka bakiye <ekle\|cikar\|ayarla> <oyuncu> <miktar>`| Bir oyuncunun bakiyesini yönetir. | `benthkese.admin.bakiye` |

## 📊 PlaceHolderAPI Placeholder'ları


Aşağıdaki placeholder'ları PlaceHolderAPI destekleyen herhangi bir eklentide kullanabilirsiniz.

### Kişisel Limit ve Bakiye
| Placeholder | Açıklama |
| --- | --- |
| `%benthkese_bakiye_formatted%` | Oyuncunun mevcut bakiyesini formatlı olarak (`1.234 ⛁`) gösterir. |
| `%benthkese_bakiye_raw%` | Oyuncunun bakiyesini formatsız, ham sayı (`1234.56`) olarak verir. |
| `%benthkese_limit_seviye_adi%` | Oyuncunun mevcut limit seviyesinin adını döndürür. |
| `%benthkese_limit_seviye_id%` | Oyuncunun mevcut limit seviyesinin sayısal ID'sini döndürür. |
| `%benthkese_limit_gonderme_kalan%` | Oyuncunun kalan günlük gönderme limitini döndürür. |
| `%benthkese_limit_gonderme_kullanilan%` | Oyuncunun o gün kullandığı gönderme miktarını gösterir. |
| `%benthkese_limit_gonderme_max%` | Oyuncunun maksimum günlük gönderme limitini gösterir. |
| `%benthkese_limit_alma_kalan%` | Oyuncunun kalan günlük alma limitini döndürür. |
| `%benthkese_limit_reset_suresi%` | Günlük limitlerin sıfırlanmasına kalan süreyi gösterir. |
| `%benthkese_limit_sonraki_seviye_adi%` | Oyuncunun bir sonraki limit seviyesinin adını döndürür. |
| `%benthkese_limit_sonraki_seviye_ucret%` | Bir sonraki seviyeye yükseltme maliyetini döndürür. |
| `%benthkese_limit_sonraki_seviye_ilerleme%`| Oyuncunun bir sonraki seviyeye ne kadar yakın olduğunu yüzde olarak gösterir.|
| `%benthkese_limit_yukseltebilir_mi%` | Oyuncunun seviye yükseltip yükseltemeyeceğini (`Evet`/`Hayır`) gösterir. |

### Vadeli Faiz Sistemi
| Placeholder | Açıklama |
| --- | --- |
| `%benthkese_faiz_hesap_sayisi%` | Oyuncunun aktif vadeli hesaplarının sayısını gösterir. |
| `%benthkese_faiz_hesap_durum%` | Oyuncunun hesap durumunu `mevcut / max` formatında gösterir. |
| `%benthkese_faiz_yatirim_toplam%` | Oyuncunun tüm vadeli hesaplarındaki toplam anapara miktarını gösterir. |
| `%benthkese_faiz_sonraki_kazanc_miktar%` | Vadesi en yakın olan hesaptan ne kadar para kazanılacağını gösterir. |
| `%benthkese_faiz_sonraki_kazanc_sure%` | Vadesi en yakın olan hesabın ne zaman dolacağını gösterir. |

### Kişisel İstatistikler
| Placeholder | Açıklama |
| --- | --- |
| `%benthkese_toplam_islem_sayisi%` | Oyuncunun yaptığı toplam işlem sayısını gösterir. |
| `%benthkese_gonderilen_toplam_para%` | Oyuncunun bugüne kadar gönderdiği toplam para miktarını gösterir. |
| `%benthkese_odenen_toplam_vergi%` | Oyuncunun bugüne kadar ödediği toplam vergi miktarını gösterir. |
| `%benthkese_siralama_bakiye%` | Oyuncunun sunucudaki bakiye sıralamasını gösterir. |
| `%benthkese_siralama_bakiye_hedef_kalan%` | Sıralamada bir üstündeki oyuncuyu geçmek için gereken para miktarını gösterir. |

### Sunucu Liderlik Tabloları (SQL Gerekli)
| Placeholder | Açıklama |
| --- | --- |
| `%benthkese_top_bakiye_isim_<1-10>%` | Sunucudaki en zengin X. oyuncunun adını gösterir. |
| `%benthkese_top_bakiye_deger_<1-10>%` | Sunucudaki en zengin X. oyuncunun bakiyesini gösterir. |
| `%benthkese_top_seviye_isim_<1-10>%` | En yüksek limit seviyesine sahip X. oyuncunun adını gösterir. |
| `%benthkese_top_seviye_deger_<1-10>%` | En yüksek limit seviyesine sahip X. oyuncunun seviye adını gösterir. |

### Sunucu Bilgileri
| Placeholder | Açıklama |
| --- | --- |
| `%benthkese_ekonomi_item_adi%` | Ekonomide kullanılan fiziksel eşyanın adını gösterir. |
| `%benthkese_vergi_yatirma_oran_yuzde%` | Para yatırma vergisinin yüzde olarak değerini gösterir. |
| `%benthkese_vergi_cekme_oran_yuzde%` | Para çekme vergisinin yüzde olarak değerini gösterir. |
| `%benthkese_vergi_gonderme_oran_yuzde%` | Para gönderme vergisinin yüzde olarak değerini gösterir. |

## ⚙️ Yapılandırma

Eklenti, üç ana yapılandırma dosyası ile gelir:

*   **`config.yml`:** Ana ayarlar. Depolama türü (`storage`), ekonomi materyali (`economy-item`), vergiler (`taxes`) ve faiz oranları (`interest`) buradan yönetilir.
*   **`messages.yml`:** Eklentideki tüm metinler. Tamamen özelleştirilebilir.
*   **`limits.yml`:** Eklentinin kalbi. Kendi limit seviyelerinizi, isimlerini, maliyetlerini ve limit değerlerini buradan oluşturun.

**Örnek `limits.yml` yapısı:**
```yaml
limit-levels:
  1:
    name: "&7Başlangıç"
    cost: 1000.0
    send-limit: 5000.0
    receive-limit: 10000.0
  2:
    name: "&aTüccar"
    cost: 5000.0
    send-limit: 25000.0
    receive-limit: 50000.0
  3:
    name: "&dBaron"
    cost: 0.0 
    send-limit: -1.0 # -1 sonsuz limit anlamına gelir
    receive-limit: -1.0
```

## 🤝 Destek ve Katkıda Bulunma

Bir hata bulursanız veya bir özellik önermek isterseniz, lütfen bu projenin **[Issues](https://github.com/your-username/BenthKese/issues)** sekmesini kullanın. Katkıda bulunmak isterseniz, pull request'ler her zaman memnuniyetle karşılanır.

## 📜 Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Daha fazla bilgi için `LICENSE` dosyasına bakın.