<?php

  require_once('koneksi.php');

  $transaksi_id     = $_POST['transaksi_id'];
  $status           = $_POST['status'];
  $jumlah           = $_POST['jumlah'];
  $keterangan       = $_POST['keterangan'];
  $tanggal          = $_POST['tanggal'];

  // echo "UPDATE transaksi SET status='$status', jumlah='$jumlah', keterangan='$keterangan'
  //   ,tanggal='$tanggal' WHERE transaksi_id ='transaksi_id'"; die;

  $sql = mysqli_query($conn, "UPDATE transaksi SET status='$status', jumlah='$jumlah', keterangan='$keterangan', tanggal='$tanggal' WHERE transaksi_id ='$transaksi_id'");

    // respon berhasil atau belum
    if ($sql) {
      echo json_encode(array('response' => 'success' ));
    } else {
      echo json_encode(array('response' => 'failed' ));
    }
?>
