# 🤝 Katkıda Bulunma Rehberi

Bu projeye katkıda bulunmak istediğiniz için teşekkürler! Lütfen aşağıdaki kurallara uyun.

## 📌 Temel Kurallar

1. **`main` branch'ına direkt push yapmayın.** Her değişiklik Pull Request ile gelir.
2. Her PR için **en az 1 kişinin onayı** (code review) gereklidir.
3. PR açmadan önce `./gradlew build` komutunun başarıyla geçtiğinden emin olun.
4. Conflict varsa PR sahibi çözer.

## 🌿 Branch İsimlendirme

| Branch Türü | Format | Örnek |
|---|---|---|
| Yeni özellik | `feature/<kısa-açıklama>` | `feature/sabotaj-mekanigi` |
| Hata düzeltme | `fix/<sorun-açıklaması>` | `fix/prizma-isik-sayisi` |
| Deneysel | `experiment/<ne-deneniyor>` | `experiment/particle-efektleri` |
| Kişisel prototip | `dev/<isim>/<konu>` | `dev/emin/yeni-ayna-tipi` |

## 💬 Commit Mesajı Kuralları

Commit mesajlarınızda aşağıdaki prefix'leri kullanın:

| Prefix | Kullanım | Örnek |
|---|---|---|
| `feat:` | Yeni özellik | `feat: prizma renk kırılması eklendi` |
| `fix:` | Hata düzeltme | `fix: 4 ışın sorunu çözüldü` |
| `refactor:` | Kod yapısı değişikliği | `refactor: Player sınıfı bölündü` |
| `docs:` | Dokümantasyon | `docs: README güncellendi` |
| `style:` | Biçimlendirme | `style: boşluk ve girintiler düzeltildi` |
| `test:` | Test ekleme | `test: Mirror yansıma testi eklendi` |

## 🔄 Çalışma Akışı

```bash
# 1. main'i güncelle
git checkout main
git pull origin main

# 2. Yeni feature branch aç
git checkout -b feature/benim-ozelligim

# 3. Değişiklik yap, test et
./gradlew build
./gradlew lwjgl3:run

# 4. Commit at
git add .
git commit -m "feat: oyuncu fırlatma mekaniği eklendi"

# 5. Push et
git push origin feature/benim-ozelligim

# 6. GitHub'da Pull Request aç
gh pr create --title "Oyuncu fırlatma mekaniği" --body "Açıklama..."
```

## 🌳 Git Worktree Kullanımı (Opsiyonel)

Aynı anda birden fazla branch üzerinde çalışmak için worktree kullanabilirsiniz:

```bash
# Ana repo dizinindeyken:
git worktree add ../Oyun-worktrees/sabotaj -b feature/sabotaj-mekanigi

# O worktree'de çalış
cd ../Oyun-worktrees/sabotaj
./gradlew build && ./gradlew lwjgl3:run

# İşin bittiğinde temizle
cd ~/Documents/Projelerim/Oyun
git worktree remove ../Oyun-worktrees/sabotaj
```

## ✅ PR Kontrol Listesi

PR açmadan önce kontrol edin:

- [ ] `./gradlew build` başarıyla geçiyor mu?
- [ ] Commit mesajları kurallara uygun mu?
- [ ] Branch ismi doğru formatta mı?
- [ ] Değişiklikler açık ve anlaşılır mı?
- [ ] Gereksiz dosya (`.class`, `.gradle/`, IDE dosyaları) commit'e dahil değil mi?

## 🏗️ Proje Yapısı Hakkında

- **`core/`**: Platform bağımsız oyun mantığı. Yeni oyun sınıflarını buraya ekleyin.
- **`lwjgl3/`**: Desktop launcher. Genellikle dokunmanıza gerek kalmaz.
- **`assets/`**: Gelecekte eklenecek ses/görüntü dosyaları için.
- Tüm oyun grafikleri `ShapeRenderer` ile çizilir, sprite gerekmez.
