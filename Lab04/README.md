# Lab04 — Тестування з Mockito, AssertJ та PIT (Іванов)

## Тема: Обробка замовлень (склад + платіжний шлюз + сповіщення)

---

## Структура проєкту

```
Lab04/
├── pom.xml                                  ← Maven залежності + PIT плагін
└── src/
    ├── main/java/ua/edu/ukma/ivanov/order/
    │   ├── Order.java                       ← Доменна модель замовлення
    │   ├── OrderItem.java                   ← Елемент замовлення
    │   ├── OrderResult.java                 ← Результат обробки (success/failure)
    │   ├── OrderService.java                ← Основна бізнес-логіка
    │   ├── WarehouseService.java            ← Інтерфейс складу
    │   ├── PaymentGateway.java              ← Інтерфейс платіжного шлюзу
    │   └── NotificationService.java         ← Інтерфейс сповіщень
    └── test/java/ua/edu/ukma/ivanov/order/
        ├── OrderServiceTest.java            ← Головний тест (Вимоги 1–4)
        ├── DiscountMutationWeakTest.java    ← Слабкий PIT тест (Вимога 5)
        └── DiscountMutationFixedTest.java   ← Виправлений PIT тест (Вимога 5)
```

---

## Покриття вимог

### Вимога 1 — Mockito: @ExtendWith, @Mock, @InjectMocks, when().thenReturn()

`OrderServiceTest.java` — клас `BusinessScenarios`:

| Сценарій | Опис |
|----------|------|
| Scenario 1 | Успішна обробка: склад ОК + оплата ОК → CONFIRMED |
| Scenario 2 | Немає на складі → CANCELLED, оплата не стягується |
| Scenario 3 | Оплата відхилена → CANCELLED, резервування не відбувається |
| Scenario 4 | Скасування підтвердженого замовлення → повернення коштів |

### Вимога 2 — void-методи: verify, times, never

`OrderServiceTest.java` — клас `VoidMethodVerification`:

| Тест | Перевірка |
|------|-----------|
| `verify` | `sendOrderConfirmation` викликається після успіху |
| `times(1)` | `sendOutOfStockNotification` викликається рівно 1 раз |
| `never` | `charge` ніколи не викликається, якщо немає товару на складі |
| `times(1)` | `reserveStock` викликується рівно 1 раз при успіху |

### Вимога 3 — AssertJ SoftAssertions

`OrderServiceTest.java` — клас `SoftAssertionTests`:
- Перевіряє всі поля результату та замовлення одночасно
- При провалі показує **всі** помилки разом

### Вимога 4 — AssertJ перевірки списків (5 типів)

`OrderServiceTest.java` — клас `ListAssertionTests`:

| Тип перевірки | Метод AssertJ |
|---------------|---------------|
| Розмір | `hasSize(3).isNotEmpty()` |
| Витяг значень | `extracting(Order::getStatus).containsOnly(CONFIRMED)` |
| Порядок елементів | `extracting(Order::getId).containsExactlyInAnyOrder(...)` |
| Предикат allMatch | `allMatch(o -> o.getTotalAmount() > 0)` |
| Предикат noneMatch | `noneMatch(o -> o.getStatus() == CANCELLED)` |

### Вимога 5 — PIT мутаційне тестування

`calculateDiscount()` має **4 гілки** (if/else if/else if/else):
- `>= 5000` → 15%
- `>= 1000` → 10%
- `>= 500` → 5%
- інакше → 0%

**`DiscountMutationWeakTest`** — тестує лише граничні значення (5000, 1000, 500).
PIT мутант "changed conditional boundary" (`>=` → `>`) **виживає**, бо
значення нижче межі не перевіряються.

**`DiscountMutationFixedTest`** — тестує значення **вище і нижче** кожної межі.
Вбиває наступні мутанти PIT:
- `changed conditional boundary` (>= → >)
- `negated conditional`
- `removed conditional`

---

## Запуск

### Запустити тести
```bat
run_tests.bat
```
або
```powershell
# якщо JAVA_HOME налаштовано правильно:
..\mvnw.cmd test --no-transfer-progress
```

### Запустити PIT мутаційне тестування
```bat
run_pit.bat
```
або
```powershell
..\mvnw.cmd org.pitest:pitest-maven:mutationCoverage --no-transfer-progress
```

Звіт: `target/pit-reports/index.html`

---

## Залежності

| Бібліотека | Версія | Призначення |
|------------|--------|-------------|
| JUnit Jupiter | 5.10.2 | Тестовий фреймворк |
| Mockito Core | 5.11.0 | Мок-об'єкти |
| Mockito JUnit Jupiter | 5.11.0 | Інтеграція з JUnit 5 |
| AssertJ Core | 3.25.3 | Fluent assertions |
| PIT | 1.15.3 | Мутаційне тестування |
| pitest-junit5-plugin | 1.2.1 | Підтримка JUnit 5 в PIT |
