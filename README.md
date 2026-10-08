# 🎮 Lichtstrahlen — 2 Oyunculu Kooperatif Bulmaca Oyunu

Java ve libGDX ile geliştirilmiş, 2 oyunculu kooperatif bir lazer yansıtma bulmaca oyunu.

## 🧩 Oyun Hakkında

İki oyuncu bir arada çalışarak (veya birbirini sabote ederek!) sabit bir lazer kaynağından çıkan ışını **aynalar** ve **üçgen prizma** aracılığıyla haritanın diğer ucundaki **renkli hedef alıcılara** yönlendirmelidir.

### Özellikler

- **Sabit Lazer Kaynağı**: Sol duvardaki sabit kaynak sürekli beyaz lazer ışını yayar.
- **Üçgen Prizma**: Beyaz lazeri kırmızı ve mavi olmak üzere iki ayrı renge ayırır.
- **Hareketli & Döndürülebilir Aynalar**: Oyuncular tarafından taşınıp hassas açıyla döndürülebilir.
- **Renkli Hedef Alıcılar**: Kırmızı alıcı sadece kırmızı, mavi alıcı sadece mavi lazerle aktif olur.
- **2 Oyunculu Kooperatif/Sabotaj**: Aynı klavyeden iki oyuncu aynı anda oynayabilir.

## 🛠️ Teknoloji

| Bileşen | Teknoloji |
|---|---|
| Dil | Java 11+ |
| Oyun Motoru | libGDX 1.13.1 |
| Desktop Backend | LWJGL3 |
| Build Sistemi | Gradle 8.7 |
| Grafik | ShapeRenderer + SpriteBatch (piksel karakter görselleri) |

## 📋 Gereksinimler

- **Java JDK 11** veya üstü (JDK 21 önerilir)
- **Git** (sürüm kontrolü için)

> **Not:** Gradle wrapper projeye dahildir, ayrıca Gradle yüklemenize gerek yoktur.

## 🚀 Kurulum & Çalıştırma

```bash
# 1. Repoyu klonla
git clone https://github.com/eminmemioglu/LazerPuzzle.git
cd LazerPuzzle

# 2. Derle
./gradlew build

# 3. Oyunu başlat
./gradlew lwjgl3:run
```

> **macOS kullanıcıları:** `-XstartOnFirstThread` JVM argümanı Gradle task'ında otomatik olarak ayarlanmıştır.

## 🎮 Kontroller

Oyun açıldığında ana menü görünür. **Oyna** bölüm seçme ekranını açar.
**1, 2, 3, 4** düğmelerinden biri seçildiğinde ilgili bölüm baştan başlar.
Bölüm seçimi ana menüyle aynı piksel laboratuvar temasını kullanır. Dört büyük
kartta bölüm numarası ve gerçek başlangıç yerleşiminden platform, ayna, prizma
ve hedef önizlemesi bulunur; kartın herhangi bir yerine tıklamak bölümü açar.
**Geri** veya **ESC** ana menüye döndürür. Dört bölüm de doğrudan seçilebilir.
İlk bölüm platformlar üzerinden ulaşılan mevcut bulmacadır; diğer üç bölüm farklı geçici prizma, ayna ve
hedef yerleşimleri kullanır. Bölüm tasarımları ve zorluk dengesi daha sonra hazırlanacaktır.
Ana menü, PixelLab laboratuvar arka planı, piksel yazı tipi ve iki animasyonlu robotla
**Lichtstrahlen** adını gösterir. Sağ üstteki **nota ikonu** menü müziğini açar/kapatır;
kapalı durumda ikonun üzerinde çizgi görünür. Yanındaki **dişli ikonu** aynı tercihin
bulunduğu ayarlar penceresini açar. Tercih uygulama yeniden açıldığında da korunur.
Özgün 24 saniyelik müzik ana menüde ve bölüm seçiminde döngü halinde çalar;
bölüme girildiğinde duraklar. Görseller `ui/lichtstrahlen/`, müzik `audio/` kaynaklarındadır.
`scripts/create_menu_support.py` piksel yazı tipini ve sentezlenmiş müziği yeniden üretir.

Oyun sırasında **ESC** ile bölüm seçimine dönülür. Bölüm seçiminde **Geri**
veya **ESC** ana menüye döndürür. Ayarlar penceresi **Kapat** veya **ESC** ile kapanır.

### Oyuncu 1 (Mavi Robot)

PixelLab'den alınan 48×48 karakter paketi ilk oyuncuda kullanılır. Başlangıçta
öne bakar; A/D ile sola/sağa döner ve yatay hareket tuşu bırakıldığında öne döner.
Yerde dururken önden nefes alma animasyonu oynar: dört kare, kare başına
200 ms, sürekli döngü. Hareket veya zıplama başladığında bekleme animasyonu
kesilir; iniş animasyonu tamamlanınca ve karakter durunca yeniden başlar.
Yerde sağa hareket ederken sekiz karelik koşma animasyonu, sola giderken onun
aynalanmış hâli oynar. Koşma döngüsü oyun hızına uygun olarak 80 ms/kare kullanır.
Duvar önünde ilerleyemiyorsa koşma döngüsü durur. Önden ve sağdan görünüşlü GIF zıplama animasyonları
yükseliş, düşüş ve kısa iniş pozlarıyla fizik hareketine bağlanmıştır. Sağa bakarken
east animasyonu, sola bakarken onun aynalanmış hâli kullanılır. Yatay hareket
olmadan yerinde zıplarken south animasyonu oynar. Havada yön değiştirmek animasyonu
yeniden başlatmaz; yatay hareket tuşu bırakılırsa ön animasyona geçer.
İniş sonrasında hareket etmiyorsa önden duran görsele döner.
GIF'in kaynak dosyası, PNG kare şeridi ve kare bilgileri kaynaklara dahildir.
Saydam kenarlar çizim sırasında ayıklanır, ayaklar zemine
hizalanır ve mevcut 34×34 çarpışma alanı korunur.

| Tuş | Eylem |
|---|---|
| `A` / `D` | Sola / sağa yürü |
| `W` | Yerdeyken zıpla |
| `SPACE` (Boşluk) | Yakındaki aynayı/prizmayı tut ve taşı |
| `Q` / `E` | Döndür (tek basış: 3.5° minik adım, basılı tutma: akıcı dönüş) |

### Oyuncu 2 (Yeşil Robot)

İkinci oyuncu da PixelLab robot görsellerini kullanır. Dururken önden dört karelik
nefes alma, sağa/sola giderken sekiz karelik koşma animasyonu oynar. Yedi karelik
zıplama animasyonu önden ve sağ profilden yüklenir; sol profil aynalanır.
Zıplamada yükseliş kareleri 0–4, düşüş karesi 5, yere temas sonrası iniş karesi 6'dır.
İki robotun animasyon süreleri birbirinden bağımsızdır; fizik ve kontroller aynıdır.
Kaynak GIF'ler, PNG kare şeritleri ve kare bilgileri
`core/src/main/resources/characters/green-robot/` altında bulunur.
Sabit ön poz nefes alma GIF'inin, sağ poz zıplama GIF'inin ilk karesinden alınmıştır;
sol poz sağ pozun aynasıdır. Saydamlık ve 34×34 çarpışma alanı korunur.

| Tuş | Eylem |
|---|---|
| `←` / `→` | Sola / sağa yürü |
| `↑` | Yerdeyken zıpla |
| `ENTER` | Yakındaki aynayı/prizmayı tut ve taşı |
| `K` / `L` | Döndür (tek basış: 3.5° minik adım, basılı tutma: akıcı dönüş) |

Karakterler yerçekimiyle düşer ve platformlara basar. Zıplama tuşuna basılı
tutmak uçurmaz veya tekrar zıplatmaz; yeniden zıplamak için yere inip tuşa
tekrar basmak gerekir. İlk bölümde prizma ve aynalara ulaşmak için basamaklar
bulunur; diğer bölüm taslaklarında şimdilik yalnızca zemin vardır. Yerçekimi
karakterlere uygulanır; aynalar ve prizma mevcut taşıma davranışını korur.

Karakterler birbirlerinin içinden geçemez. Yandan yürüyerek birbirlerini
itebilir; karşılıklı aynı güçte itişince dururlar. Diğer oyuncunun üstüne
zıplayıp üzerinde durabilir, onunla birlikte taşınabilir ve üzerinden tekrar
zıplayabilirler. Alttaki oyuncu da üstündeki oyuncuyla birlikte zıplayabilir.

## 📁 Proje Yapısı

```
├── core/                          # Oyun mantığı (platform bağımsız)
│   └── src/main/java/com/mygame/
│       ├── MainGame.java          # libGDX yaşam döngüsü & ekran geçişleri
│       ├── MenuScreen.java        # Tıklanabilir ana menü & ayarlar taslağı
│       ├── LevelSelectScreen.java # 1–4 bölüm seçimi
│       ├── LevelDefinition.java   # Bölümlerin başlangıç yerleşimleri
│       ├── MenuSkin.java          # Menülerin ortak geçici görünümü
│       ├── GameScreen.java        # Ana oyun ekranı
│       ├── Player.java            # Oyuncu kontrolü & etkileşim
│       ├── PlayerSprite.java      # PixelLab robotunun yön görselleri
│       ├── Platform.java          # Katı zemin ve basamak sınırları
│       ├── Mirror.java            # Hareketli & döndürülebilir ayna
│       ├── Prism.java             # Üçgen optik prizma
│       ├── LaserSystem.java       # Sabit lazer & ışın izleme
│       └── TargetReceiver.java    # Renkli hedef alıcı
├── lwjgl3/                        # Desktop launcher
│   └── src/main/java/com/mygame/lwjgl3/
│       └── Lwjgl3Launcher.java
├── build.gradle                   # Root Gradle config
├── settings.gradle
└── gradle.properties
```

## 🤝 Katkıda Bulunma

Katkı kuralları için [CONTRIBUTING.md](CONTRIBUTING.md) dosyasına bakın.

## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
