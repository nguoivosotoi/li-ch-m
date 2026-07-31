import json

with open('app/src/main/assets/lunar_data.json', 'r') as f:
    data = json.load(f)

# Flatten into a list of days to easily compute backwards/forwards
# We know when month/year/isleap changes, day is 1.
# Let's iterate backwards to fill the first month, then forwards.

flat_list = []
for y in sorted([int(k) for k in data.keys()]):
    year_str = str(y)
    for m in range(1, 13):
        month_str = str(m)
        if month_str in data[year_str]:
            for d_idx, day_data in enumerate(data[year_str][month_str]):
                flat_list.append(day_data)

# Forward pass
current_day = 1
for i in range(len(flat_list)):
    if i == 0:
        continue # skip the first one for now
    prev = flat_list[i-1]
    curr = flat_list[i]
    if curr[1] != prev[1] or curr[2] != prev[2] or curr[3] != prev[3]:
        # month changed
        curr[0] = 1
    else:
        if prev[0] is not None:
            curr[0] = prev[0] + 1

# Backward pass for the very beginning
for i in range(len(flat_list) - 1, -1, -1):
    curr = flat_list[i]
    if curr[0] is None:
        if i + 1 < len(flat_list):
            curr[0] = flat_list[i+1][0] - 1

# Re-assign to dict
idx = 0
for y in sorted([int(k) for k in data.keys()]):
    year_str = str(y)
    for m in range(1, 13):
        month_str = str(m)
        if month_str in data[year_str]:
            for d_idx in range(len(data[year_str][month_str])):
                data[year_str][month_str][d_idx] = flat_list[idx]
                idx += 1

with open('app/src/main/assets/lunar_data.json', 'w') as f:
    json.dump(data, f, separators=(',', ':'))
