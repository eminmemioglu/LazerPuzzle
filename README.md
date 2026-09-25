# 🎮 Lazer Puzzle — 2 Oyunculu Kooperatif Bulmaca Oyunu

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
| Grafik | ShapeRenderer (sprite gerektirmez) |

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

### Oyuncu 1 (Mavi Kutu)

| Tuş | Eylem |
|---|---|
| `W` `A` `S` `D` | Hareket |
| `SPACE` (Boşluk) | Yakındaki aynayı/prizmayı tut ve taşı |
| `Q` / `E` | Döndür (tek basış: 3.5° minik adım, basılı tutma: akıcı dönüş) |

### Oyuncu 2 (Yeşil Kutu)

| Tuş | Eylem |
|---|---|
| `↑` `←` `↓` `→` | Hareket |
| `ENTER` | Yakındaki aynayı/prizmayı tut ve taşı |
| `K` / `L` | Döndür (tek basış: 3.5° minik adım, basılı tutma: akıcı dönüş) |

## 📁 Proje Yapısı

```
├── core/                          # Oyun mantığı (platform bağımsız)
│   └── src/main/java/com/mygame/
│       ├── MainGame.java          # libGDX yaşam döngüsü
│       ├── GameScreen.java        # Ana oyun ekranı
│       ├── Player.java            # Oyuncu kontrolü & etkileşim
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
