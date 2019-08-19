<?php

  require_once('koneksi.php');

  $transaksi_id     = $_POST['transaksi_id'];

  $sql = mysqli_query($conn, "DELETE FROM transaksi WHERE transaksi_id='$transaksi_id'");

    // respon berhasil atau belum
    if ($sql) {
      echo json_encode(array('response' => 'success' ));
    } else {
      echo json_encode(array('response' => 'failed' ));

    }

?>
