<?php 

    require_once('koneksi.php');

    $status = $_POST['status'];
    $jumlah = $_POST['jumlah'];
    $keterangan = $_POST['keterangan'];

    $tanggal = date('Y-m-d');

    $sql = mysqli_query($conn,"INSERT INTO transaksi (status, jumlah, keterangan,tanggal) VALUES (
        '$status', '$jumlah', '$keterangan','$tanggal')");
    
        // respon berhasil atau belum
        if ($sql) {
          echo json_encode(array('response' => 'success' ));
        } else {
          echo json_encode(array('response' => 'failed' ));
    
        }
?>