# Download data

```bash
cd data/task4-train
wget http://poleval.pl/task4/task4-train.zip
unzip task4-train.zip
```

# Compile

```bash
./mvnw clean package
```

# Evaluation

```bash
./clex eval -T 32 -i data/task4-train/index-hocr.list -m data/task4-train/ground_truth-train-v1.csv -o report.tsv

```

Expected output:
```bash
                Type |  True | False | Accuracy
--------------------------------------------------------------------------------
              person |   510 |  9282 |     5.21%
         period_from |  1611 |    46 |    97.22%
             company |  1340 |   317 |    80.87%
        drawing_date |   614 |  1043 |    37.05%
           period_to |  1611 |    46 |    97.22%
--------------------------------------------------------------------------------
               TOTAL |  5686 | 10734 |    34.63%
```
