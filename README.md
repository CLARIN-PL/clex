# Download data

```bash
cd data/task4-train
wget http://poleval.pl/task4/task4-train.zip
unzip task4-train.zip
```

# Compile

```bash

```

# Evaluation

```bash
./clex eval -T 32 -i data/task4-train/index-hocr.list -m data/task4-train/ground_truth-train-v1.csv -o report.tsv

```

Expected output:
```bash
                Type |  True | False | Accuracy
--------------------------------------------------------------------------------
              person |  1498 |  7406 |    16.82%
         period_from |  1611 |    48 |    97.11%
             company |  1388 |   271 |    83.66%
        drawing_date |   543 |  1116 |    32.73%
           period_to |  1611 |    48 |    97.11%
--------------------------------------------------------------------------------
               TOTAL |  6651 |  8889 |    42.80%
```
