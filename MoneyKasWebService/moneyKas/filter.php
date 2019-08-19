<?php

  require_once('koneksi.php');

  $dariTanggal = $_GET['dari'];
  $keTanggal = $_GET['ke'];

  // untuk memfilter tanggal
  $sql = mysqli_query($conn,
   "SELECT * FROM TRANSAKSI WHERE (tanggal >= '$dariTanggal') AND (tanggal <='$keTanggal') ORDER BY transaksi_id DESC");

    // jika berhasil membaca data dari sql
    // maka hasil pepmbacaan datanya dimasukkan ke dalam array hasil
    $hasil = array();
    while ($baris = mysqli_fetch_array($sql)) {

      $date = date_create( $baris['tanggal']);
      $tanggal = date_format( $date, "d/m/Y");

      array_push($hasil, array(
        'transaksi_id'  => $baris['transaksi_id'],
        'status'        => $baris['status'],
        'jumlah'        => $baris['jumlah'],
        'keterangan'    => $baris['keterangan'],
        'tanggal'       => $tanggal,
'tanggal2'       => $baris['tanggal'],
      ));
    }

    // untuk memfilter jumlah
    $str_query = "SELECT (SELECT SUM(jumlah) from transaksi WHERE status='masuk' AND (tanggal >= '$dariTanggal') AND (tanggal <='$keTanggal')) AS masuk,
    (SELECT SUM(jumlah) from transaksi WHERE status='keluar' AND (tanggal >= '$dariTanggal') AND (tanggal <='$keTanggal')) AS keluar";
    $sql = mysqli_query($conn,$str_query );

    while ($baris = mysqli_fetch_array($sql)) {
    $masuk  = $baris['masuk'];
    $keluar  = $baris['keluar'];
    }

    echo json_encode(array(
      'masuk'    => $masuk == null ? '0' : $masuk,
      'keluar'   => $keluar == null ? '0' : $keluar,
      'saldo'   => ($masuk - $keluar),
      'hasil'    => $hasil ));
?>
