function buildChart(labels, data) {
  var ctx = document.getElementById("chartCanvas").getContext("2d");
  var myChart = new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [
        {
          label: "NO partitioning",
          data: data[0],
          fill: false,
          borderColor: "rgb(75, 192, 192)",
          tension: 0.4,
        },
        {
          label: "WITH partitioning",
          data: data[1],
          fill: false,
          borderColor: "rgb(192, 72, 72)",
          tension: 0.4,
        },
      ],
    },
  });
}
