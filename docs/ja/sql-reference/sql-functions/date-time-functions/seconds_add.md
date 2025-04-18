---
displayed_sidebar: docs
---

# seconds_add

## 説明

### 構文

```Haskell
DATETIME SECONDS_ADD(DATETIME expr1,INT expr2)
```

指定された時間間隔を日付に追加します。単位は秒です。

expr1 パラメータは有効な日時式です。

expr2 パラメータは追加したい秒数です。

## 例

```Plain Text
select seconds_add('2010-11-30 23:50:50', 2);
+---------------------------------------+
| seconds_add('2010-11-30 23:50:50', 2) |
+---------------------------------------+
| 2010-11-30 23:50:52                   |
+---------------------------------------+

select seconds_add('2010-11-30', 2);
+------------------------------+
| seconds_add('2010-11-30', 2) |
+------------------------------+
| 2010-11-30 00:00:02          |
+------------------------------+
```

## キーワード

SECONDS_ADD, ADD